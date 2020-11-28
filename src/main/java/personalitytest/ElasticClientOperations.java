package personalitytest;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Map.Entry;
import java.util.TimeZone;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * This is a component class which is used to perform elastic queries over given elastic indices. It saves answers of
 * the questions which are sent by the user according to its nickname {@value Constants.NICKNAME_ATTRIBUTE} or searchs
 * for questions details or answers that are sent by the users.
 * 
 * @author gizemabali
 *
 */
@Component
public class ElasticClientOperations {

	private static final Logger logger = LogManager.getLogger(ElasticClientOperations.class);

	/**
	 * This is a rest client of elastic to perform elastic requests.
	 */
	@Autowired
	static RestHighLevelClient client;

	/**
	 * Singleton ElasticClientOperations instance.
	 */
	private final static ElasticClientOperations operations = new ElasticClientOperations();

	private ElasticClientOperations() {
	};

	/**
	 * set client
	 * 
	 * @param restClient
	 */
	@Autowired
	public void setClient(RestHighLevelClient restClient) {
		client = restClient;
	}

	public boolean closeClient() {
		boolean isClosed = false;
		try {
			client.close();
			isClosed = true;
		} catch (Exception e) {
			logger.error("could not close the client!", e);
		}
		return isClosed;
	}

	/**
	 * @return singleton ElasticClientOperations instance.
	 */
	public static ElasticClientOperations getInstance() {
		return operations;
	}

	/**
	 * This method is used to get categories from the given index of indexName in elastic search. It uses an term
	 * aggregation builder {@link TermsAggregationBuilder} to get distinct categories.
	 * 
	 * @param indexName it is the name of the index which will be searched.
	 * @return a json object that has categories.
	 * @throws Exception
	 */
	public JsonObject getCategories(String indexName) throws Exception {
		logger.debug(String.format("getting categories! indexName \"%s\"", indexName));
		JsonObject responseObj = new JsonObject();
		responseObj.add(Constants.CATEGORIES, new JsonArray());
		TermsAggregationBuilder aggregation = AggregationBuilders.terms(Constants.TOP_TAGS).field(Constants.CATEGORY);
		SearchSourceBuilder builder = new SearchSourceBuilder().aggregation(aggregation);

		SearchRequest searchRequest = new SearchRequest().indices(indexName).source(builder);
		try {
			SearchResponse response = client.search(searchRequest, RequestOptions.DEFAULT);
			Terms subRequestIdTerms = response.getAggregations().get(Constants.TOP_TAGS);
			for (Terms.Bucket subRequestIdBucket : subRequestIdTerms.getBuckets()) {
				responseObj.get(Constants.CATEGORIES).getAsJsonArray().add(subRequestIdBucket.getKeyAsString());
			}
		} catch (Exception e) {
			logger.error(Constants.ErrorMessages.COULD_NOT_SEARCH_IN_ELASTIC, e);
			throw e;
		}
		logger.info(String.format("got all categories! responseObj %s", responseObj.toString()));
		return responseObj;
	}

	/**
	 * This method is used to get question details which is related to the given category from the given index of
	 * indexName in elastic search. It searches for the questions of the given category and returns them with their
	 * question types {@value Constants.QUESTION_TYPE} and their options {@value Constants.OPTIONS}.
	 * 
	 * @param category  this information indicates question category to group all questions.
	 * @param indexName it is the name of the index which will be searched.
	 * @return a json array which stores all questions of the given category.
	 * @throws throws an Exception if any problem occur.
	 */
	public JsonArray getQuestionDetails(String category, String indexName) throws Exception {
		logger.debug(String.format("getting question details category \"%s\", indexName \"%s\"", category, indexName));
		JsonArray questionDetails = new JsonArray();
		try {
			SearchSourceBuilder builder = new SearchSourceBuilder()
					.query(QueryBuilders.termQuery(Constants.CATEGORY, category)).size(10000);

			SearchRequest searchRequest = new SearchRequest().indices(indexName).source(builder);
			SearchResponse response = null;
			try {
				response = client.search(searchRequest, RequestOptions.DEFAULT);
			} catch (Exception e) {
				logger.error(Constants.ErrorMessages.COULD_NOT_SEARCH_IN_ELASTIC, e);
			}

			SearchHit[] hits = response.getHits().getHits();
			for (SearchHit hit : hits) {
				String hitStr = hit.getSourceAsString();
				JsonObject hitObj = JsonParser.parseString(hitStr).getAsJsonObject();
				JsonObject questionObj = new JsonObject();
				questionObj.addProperty(Constants.QUESTION, hitObj.get(Constants.QUESTION).getAsString());
				questionObj.addProperty(Constants.TYPE,
						hitObj.get(Constants.QUESTION_TYPE).getAsJsonObject().get(Constants.TYPE).getAsString());
				questionObj.add(Constants.OPTIONS,
						hitObj.get(Constants.QUESTION_TYPE).getAsJsonObject().get(Constants.OPTIONS).getAsJsonArray());
				questionDetails.add(questionObj);
			}
		} catch (Exception e) {
			logger.error(Constants.ErrorMessages.UNEXPECTED_ERROR_OCCUR, e);
			throw e;
		}
		logger.info(String.format("got question details for category %s! questionDetails %s", category,
				questionDetails.toString()));
		return questionDetails;
	}

	/**
	 * This method is used to save answer details of the questions which is related to the given category to the index
	 * of the given indexName.
	 * 
	 * Saving operation can or cannot be performed according to the following conditions.
	 * <ul>
	 * <li>First, it is checked that the answer details object has nickname attribute
	 * {@value Constants.NICKNAME_ATTRIBUTE}. If it doesn't have it, error response is returned to the user.</li>
	 * <li>Secondly, answer details object is controlled if all questions are answered (they don't have empty answers)
	 * and answer object to index is created.</li>
	 * <li>In order to save the answers, answers of the questions that are related to the given category must be sent
	 * first time. In order to control it, a search question is sent to the elastic index. If any error occur, error
	 * response is returned to the user.</li>
	 * <li>If elastic operations cannot be performed the answers cannot be saved.</li>
	 * <li>Otherwise, the answers will be saved to the index related to the given indexName.</li>
	 * </ul>
	 * 
	 * @param category         this information indicates question category to group all questions.
	 * @param answerDetailsObj it is the object which has the answers and the questions.
	 * @param indexName        it is the name of the index which will be searched.
	 * @return a json object that has the information of the saving operation.
	 * @throws throws an Exception if any problem occur.
	 */
	public ResponseEntity<String> saveAnswerDetails(String category, JsonObject answerDetailsObj, String indexName)
			throws Exception {
		logger.debug(String.format("saving answer details category \"%s\", indexName \"%s\"", category, indexName));
		JsonObject responseObj = new JsonObject();
		JsonObject answerObj = new JsonObject();
		boolean isSaved = false;
		boolean isValid = true;
		Exception exception = null;
		int statusCode = 200;
		if (answerDetailsObj.has(Constants.NICKNAME_ATTRIBUTE)
				&& !answerDetailsObj.get(Constants.NICKNAME_ATTRIBUTE).getAsString().isEmpty()) {
			answerObj.addProperty(Constants.NICKNAME_ATTRIBUTE,
					answerDetailsObj.get(Constants.NICKNAME_ATTRIBUTE).getAsString());
		} else {
			responseObj.addProperty(Constants.RESPONSE,
					Constants.ErrorMessages.YOUR_ANSWERS_COULD_NOT_BE_SAVED_PLEASE_DO_NOT_ENTER_AN_EMPTY_NICKNAME);
			isValid = false;
			statusCode = 422;
		}
		if (isValid) {
			answerDetailsObj.remove(Constants.NICKNAME_ATTRIBUTE);
			answerObj.addProperty(Constants.DATE, getCurrentDate());
			answerObj.addProperty(Constants.CATEGORY, category);
			answerObj.add(Constants.ANSWERS, new JsonArray());
			isValid = validateAndCreateAnswerDocument(answerDetailsObj, responseObj, answerObj);
			if (isValid) {
				String nickname = answerObj.get(Constants.NICKNAME_ATTRIBUTE).getAsString();
				String id = nickname + "_" + category;
				try {
					String document = getById(indexName, id);
					if (document == null) {
						try {
							String docId = indexDocument(indexName, answerObj, id);
							if (docId != null) {
								responseObj.addProperty(Constants.RESPONSE, String.format(
										Constants.ErrorMessages.ANSWERS_WERE_SAVED_FOR_THE_S_CATEGORY, category));
								isSaved = true;
							} else {
								responseObj.addProperty(Constants.RESPONSE,
										Constants.ErrorMessages.UNEXPECTED_ERROR_OCCUR_PLEASE_TRY_AGAIN);
								statusCode = 500;
							}
						} catch (Exception e) {
							responseObj.addProperty(Constants.RESPONSE,
									Constants.ErrorMessages.UNEXPECTED_ERROR_OCCUR_PLEASE_TRY_AGAIN);
							exception = e;
							statusCode = 500;
						}
					} else {
						responseObj.addProperty(Constants.RESPONSE, String.format(
								Constants.ErrorMessages.ANSWERS_WERE_SAVED_BEFORE_FOR_THE_S_CATEGORY_BY_THE_NICKNAME_S_PLEASE_USE_A_DIFFERENT_NICKNAME,
								category, nickname));
						statusCode = 400;
					}
				} catch (Exception e) {
					responseObj.addProperty(Constants.RESPONSE,
							Constants.ErrorMessages.UNEXPECTED_ERROR_OCCUR_PLEASE_TRY_AGAIN);
					exception = e;
					statusCode = 500;
				}
			} else {
				statusCode = 422;
			}
		}
		String message = String.format("response: %s, index: %s, category: %s, answerDetailsObj: %s",
				responseObj.toString(), indexName, category, answerDetailsObj.toString());
		if (isSaved) {
			logger.info("Saved questions! " + message);
		} else {
			if (exception == null) {
				logger.error("Could not save answers! " + message);
			} else {
				logger.error("Could not save answers! " + message, exception);
			}
		}
		return ResponseEntity.status(statusCode).body(responseObj.toString());
	}

	/**
	 * This method is used to validate answer details that come from the users and at the same time it creates the
	 * answer document that will be saved to elasticsearch.
	 * 
	 * @param answerDetailsObj it is the object of answer details from the user by requests.
	 * @param responseObj      it is the response object that will be sent to the user.
	 * @param answerObj        it is the object of the document that will be save to index.
	 * @return a bool value that indicates if the answers are valid.
	 */
	public boolean validateAndCreateAnswerDocument(JsonObject answerDetailsObj, JsonObject responseObj,
			JsonObject answerObj) {
		boolean hasValidAnswers = true;
		for (Entry<String, JsonElement> key : answerDetailsObj.entrySet()) {
			JsonObject subAnswerObj = new JsonObject();
			subAnswerObj.addProperty(Constants.QUESTION, key.getKey());
			if (key.getValue() != null && !key.getValue().getAsString().equals("")) {
				subAnswerObj.addProperty(Constants.ANSWER, key.getValue().getAsString());
				answerObj.get(Constants.ANSWERS).getAsJsonArray().add(subAnswerObj);
			} else {
				responseObj.addProperty(Constants.RESPONSE,
						Constants.ErrorMessages.YOUR_ANSWERS_COULD_NOT_BE_SAVED_PLEASE_BE_SURE_THAT_YOU_ANSWERED_ALL_QUESTIONS);
				hasValidAnswers = false;
				break;
			}
		}
		return hasValidAnswers;
	}

	/**
	 * This method is used to index then given answerObj documents to the given index using the given id.
	 * 
	 * @param indexName it is the name of the index that document will be indexed.
	 * @param answerObj it is the answer object that will be indexed to the given index.
	 * @param id        it is the id of the document that will be used to index document into the given index.
	 * @return a doc id from the elastic search
	 * @throws Exception
	 */
	public String indexDocument(String indexName, JsonObject answerObj, String id) throws Exception {
		IndexRequest insertRequest = new IndexRequest(indexName);
		if (id != null) {
			insertRequest.id(id);
		}
		insertRequest.source(answerObj.toString(), XContentType.JSON);
		return client.index(insertRequest, RequestOptions.DEFAULT).getId();
	}

	/**
	 * This method creates the current date according to the date format {@value Constants.DATE_FORMAT}.
	 * 
	 * @return the created date format string value.
	 */
	public String getCurrentDate() {
		Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone(Constants.GMT_3));
		Date date = calendar.getTime();
		SimpleDateFormat dateFormat = new SimpleDateFormat(Constants.DATE_FORMAT);
		dateFormat.setTimeZone(TimeZone.getTimeZone(Constants.GMT_3));
		return dateFormat.format(date);
	}

	/**
	 * This method gets document which has the given id from the index of the given indexName.
	 * 
	 * @param indexName it is the name of the index which will be searched.
	 * @param id        it is the document id.
	 * @return the string value of the document.
	 * @throws throws an Exception if any problem occur.
	 */
	private String getById(String indexName, String id) throws Exception {
		String doc = null;
		GetResponse response = client.get(new GetRequest().index(indexName).id(id), RequestOptions.DEFAULT);
		if (response.isExists() && !response.isSourceEmpty()) {
			doc = response.getSourceAsString();
		}
		return doc;
	}

	/**
	 * This method gets answers sent by the user of the given nickname from the index of the given indexName.
	 * 
	 * @param nickname  it is the nickname of the user which sends answers. nickname attribute must be unique and it is
	 *                  also used to form index id.
	 * @param indexName it is the name of the index which will be searched.
	 * @return a json array that has all answers grouped by the category.
	 * @throws throws an Exception if any problem occur.
	 */
	public JsonArray getAnswers(String nickname, String indexName) throws Exception {
		BoolQueryBuilder finalBoolQuery = new BoolQueryBuilder();
		finalBoolQuery.filter(QueryBuilders.termQuery(Constants.NICKNAME_ATTRIBUTE, nickname));
		SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
		SearchSourceBuilder builder = searchSourceBuilder.query(finalBoolQuery);
		SearchRequest searchRequest = new SearchRequest(indexName);
		searchRequest.source(builder);

		SearchResponse searchResponse = null;
		JsonArray answers = new JsonArray();
		try {
			searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
			SearchHit[] searchHits = searchResponse.getHits().getHits();
			for (SearchHit hit : searchHits) {
				JsonObject documentObj = new Gson().fromJson(hit.toString(), JsonObject.class).get(Constants._SOURCE)
						.getAsJsonObject();
				answers.add(documentObj);
			}
		} catch (Exception e) {
			logger.error(Constants.ErrorMessages.GET_ANSWERS_ERROR, e);
			throw e;
		}
		return answers;
	}
}
