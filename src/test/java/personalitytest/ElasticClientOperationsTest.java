package personalitytest;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.lang.reflect.Method;
import java.net.UnknownHostException;

import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.admin.indices.refresh.RefreshRequest;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.support.master.AcknowledgedResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.CreateIndexResponse;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.client.indices.PutIndexTemplateRequest;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.xcontent.XContentType;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.mockito.Mockito;
import org.springframework.data.elasticsearch.client.ClientConfiguration;
import org.springframework.data.elasticsearch.client.RestClients;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class ElasticClientOperationsTest {

	private static final String TEST_INDEX = "test-index";

	private static final String TEST_ANSWER_INDEX = "test-answer-index";

	private RestHighLevelClient testClient = null;

	private ElasticClientOperations operations;

	@Before
	public void setUp() throws UnknownHostException, InterruptedException {
		ClientConfiguration clientConfiguration = ClientConfiguration.builder().connectedTo("localhost:9200").build();
		testClient = RestClients.create(clientConfiguration).rest();
		operations = Mockito.spy(new ElasticClientOperations());
		Mockito.when(operations.getCurrentDate()).thenReturn("2020-11-15 23:40:57");
		operations.setClient(testClient);

		if (indexAvailable(TEST_INDEX)) {
			deleteIndex(TEST_INDEX);
		}
		if (indexAvailable(TEST_ANSWER_INDEX)) {
			deleteIndex(TEST_ANSWER_INDEX);
		}
		String textIndexTemplate = "{\"index_patterns\":[\"test-index*\"],\"settings\":{},\"mappings\":{\"_source\":{\"enabled\":true},\"properties\":{\"question\":{\"type\":\"keyword\"},\"question_type\":{\"type\":\"nested\",\"properties\":{\"options\":{\"type\":\"keyword\"},\"type\":{\"type\":\"keyword\"}}},\"category\":{\"type\":\"keyword\"}}},\"aliases\":{}}";
		createTemplate("template-text-index", textIndexTemplate);
		Thread.sleep(200);
		createIndex(TEST_INDEX, null, 1);

		String textAnswerIndexTemplate = "{\"index_patterns\":[\"answer*\"],\"settings\":{\"number_of_shards\":1},\"mappings\":{\"_source\":{\"enabled\":true},\"properties\":{\"nickname\":{\"type\":\"keyword\"},\"date\":{\"type\":\"date\",\"format\":\"yyyy-MM-dd HH:mm:ss\"},\"answers\":{\"type\":\"nested\",\"properties\":{\"question\":{\"type\":\"keyword\"},\"answer\":{\"type\":\"keyword\"}}}}}}";
		createTemplate("template-test-answer-index", textAnswerIndexTemplate);
		Thread.sleep(200);
		createIndex(TEST_ANSWER_INDEX, null, 1);
	}

	@After
	public void tearDown() {

	}

	@Test
	public void getCategories_oneCategory() throws InterruptedException {
		// set up
		String initialQuestionsStr = "[{\"question\":\"What is your gender?\",\"category\":\"hard_fact\",\"question_type\":{\"type\":\"single_choice\",\"options\":[\"male\",\"female\",\"other\"]}}]";
		JsonArray initialQuestions = JsonParser.parseString(initialQuestionsStr).getAsJsonArray();
		indexDocuments(initialQuestions);
		refresh(TEST_INDEX);

		// execute
		JsonObject categories = operations.getCategories(TEST_INDEX);

		// assert
		String expectedCategories = "{\"categories\":[\"hard_fact\"]}";
		assertEquals("categories must has a hard_fact value",
				JsonParser.parseString(expectedCategories).getAsJsonObject(), categories);
	}

	@Test
	public void getCategories_twoCategory() throws InterruptedException {
		// set up
		String initialQuestionsStr = "[{\"question\":\"What is your gender?\",\"category\":\"hard_fact\",\"question_type\":{\"type\":\"single_choice\",\"options\":[\"male\",\"female\",\"other\"]}},{\"question\":\"What is your marital status?\",\"category\":\"passion\",\"question_type\":{\"type\":\"single_choice\",\"options\":[\"never married\",\"separated\",\"divorced\",\"widowed\"]}}]";
		JsonArray initialQuestions = JsonParser.parseString(initialQuestionsStr).getAsJsonArray();
		indexDocuments(initialQuestions);
		refresh(TEST_INDEX);

		// execute
		JsonObject categories = operations.getCategories(TEST_INDEX);

		System.out.println("categories: " + categories.toString());
		// assert
		String expectedCategories = "{\"categories\":[\"hard_fact\",\"passion\"]}";
		assertEquals("categories must has a hard_fact and passion value",
				JsonParser.parseString(expectedCategories).getAsJsonObject(), categories);
	}

	@Test
	public void getQuestionDetails_oneDocument() throws Exception {
		// set up
		String initialQuestionsStr = "[{\"question\":\"What is your gender?\",\"category\":\"hard_fact\",\"question_type\":{\"type\":\"single_choice\",\"options\":[\"male\",\"female\",\"other\"]}}]";
		JsonArray initialQuestions = JsonParser.parseString(initialQuestionsStr).getAsJsonArray();
		indexDocuments(initialQuestions);
		refresh(TEST_INDEX);

		// execute
		JsonArray questionDetails = operations.getQuestionDetails("hard_fact", TEST_INDEX);

		// assert
		String expectedQuestionDetails = "[{\"question\":\"What is your gender?\",\"type\":\"single_choice\",\"options\":[\"male\",\"female\",\"other\"]}]";
		assertEquals("expectedQuestionDetails must has one document",
				JsonParser.parseString(expectedQuestionDetails).getAsJsonArray(), questionDetails);
	}

	@Test
	public void getQuestionDetails_twoDocuments() throws Exception {
		// set up
		String initialQuestionsStr = "[{\"question\":\"What is your gender?\",\"category\":\"hard_fact\",\"question_type\":{\"type\":\"single_choice\",\"options\":[\"male\",\"female\",\"other\"]}},{\"question\":\"What is your marital status?\",\"category\":\"hard_fact\",\"question_type\":{\"type\":\"single_choice\",\"options\":[\"never married\",\"separated\",\"divorced\",\"widowed\"]}}]";
		JsonArray initialQuestions = JsonParser.parseString(initialQuestionsStr).getAsJsonArray();
		indexDocuments(initialQuestions);
		refresh(TEST_INDEX);

		// execute
		JsonArray questionDetails = operations.getQuestionDetails("hard_fact", TEST_INDEX);

		// assert
		String expectedQuestionDetails = "[{\"question\":\"What is your gender?\",\"type\":\"single_choice\",\"options\":[\"male\",\"female\",\"other\"]},{\"question\":\"What is your marital status?\",\"type\":\"single_choice\",\"options\":[\"never married\",\"separated\",\"divorced\",\"widowed\"]}]";
		assertEquals("expectedQuestionDetails must has two documents",
				JsonParser.parseString(expectedQuestionDetails).getAsJsonArray(), questionDetails);
	}

	@Test
	public void getQuestionDetails_twoRelatedOneUnrelatedDocuments() throws Exception {
		// set up
		String initialQuestionsStr = "[{\"question\":\"What is your gender?\",\"category\":\"hard_fact\",\"question_type\":{\"type\":\"single_choice\",\"options\":[\"male\",\"female\",\"other\"]}},{\"question\":\"What is your marital status?\",\"category\":\"hard_fact\",\"question_type\":{\"type\":\"single_choice\",\"options\":[\"never married\",\"separated\",\"divorced\",\"widowed\"]}},{\"question\":\"How often do you smoke?\",\"category\":\"lifestyle\",\"question_type\":{\"type\":\"single_choice\",\"options\":[\"never\",\"once or twice a year\",\"socially\",\"frequently\"]}}]";
		JsonArray initialQuestions = JsonParser.parseString(initialQuestionsStr).getAsJsonArray();
		indexDocuments(initialQuestions);
		refresh(TEST_INDEX);

		// execute
		JsonArray questionDetails = operations.getQuestionDetails("hard_fact", TEST_INDEX);

		// assert
		String expectedQuestionDetails = "[{\"question\":\"What is your gender?\",\"type\":\"single_choice\",\"options\":[\"male\",\"female\",\"other\"]},{\"question\":\"What is your marital status?\",\"type\":\"single_choice\",\"options\":[\"never married\",\"separated\",\"divorced\",\"widowed\"]}]";
		assertEquals("expectedQuestionDetails must has two documents",
				JsonParser.parseString(expectedQuestionDetails).getAsJsonArray(), questionDetails);
	}

	@Test
	public void saveAnswerDetails_firstSave() throws Exception {
		// set up
		JsonObject answerDetailsObj = new JsonObject();
		answerDetailsObj.addProperty(Constants.NICKNAME_ATTRIBUTE, "nick");
		answerDetailsObj.addProperty("How are you?", "good!");
		// execute
		JsonObject answerDetails = operations.saveAnswerDetails("lifestyle", answerDetailsObj, TEST_INDEX);

		String documentStr = getById(TEST_INDEX, "nick_lifestyle");
		// assert
		String expectedDocument = "{\"nickname\":\"nick\",\"date\":\"2020-11-15 23:40:57\",\"category\":\"lifestyle\",\"answers\":[{\"question\":\"How are you?\",\"answer\":\"good!\"}]}";

		assertEquals("expectedDocument must has fields!", JsonParser.parseString(expectedDocument).getAsJsonObject(),
				JsonParser.parseString(documentStr).getAsJsonObject());

		String expectedAnswerTetails = "{\"response\":\"Answers were saved for the lifestyle category!\"}";
		assertEquals("answer details has to have the success response!",
				JsonParser.parseString(expectedAnswerTetails).getAsJsonObject(), answerDetails);
	}

	@Test
	public void saveAnswerDetails_secondSaveAttemptFailure() throws Exception {
		// set up
		String initialQuestionsStr = "{\"nickname\":\"nick\",\"date\":\"2020-11-15 23:40:57\",\"category\":\"lifestyle\",\"answers\":[{\"question\":\"How are you?\",\"answer\":\"good!\"}]}";
		operations.indexDocument(TEST_INDEX, JsonParser.parseString(initialQuestionsStr).getAsJsonObject(),
				"nick_lifestyle");
		refresh(TEST_INDEX);

		JsonObject answerDetailsObj = new JsonObject();
		answerDetailsObj.addProperty(Constants.NICKNAME_ATTRIBUTE, "nick");
		answerDetailsObj.addProperty("How are you?", "good!");
		// execute
		JsonObject answerDetails = operations.saveAnswerDetails("lifestyle", answerDetailsObj, TEST_INDEX);

		// assert
		String expectedAnswerTetails = "{\"response\":\"Answers were saved before for the lifestyle category by the nickname nick. Please use a different nickname!\"}";
		assertEquals("answer details has to have the success response!",
				JsonParser.parseString(expectedAnswerTetails).getAsJsonObject(), answerDetails);
	}

	@Test
	public void indexDocument() throws Exception {
		// set up
		JsonObject answerDetailsObj = new JsonObject();
		answerDetailsObj.addProperty(Constants.NICKNAME_ATTRIBUTE, "nick");
		answerDetailsObj.addProperty("How are you?", "good!");
		// execute
		String id = operations.indexDocument(TEST_INDEX, answerDetailsObj, "test-id");

		refresh(TEST_INDEX);
		String documentStr = getById(TEST_INDEX, "test-id");

		// assert
		String expectedDocument = "{\"nickname\":\"nick\",\"How are you?\":\"good!\"}";
		assertEquals("answer details has to have the success response!",
				JsonParser.parseString(expectedDocument).getAsJsonObject(),
				JsonParser.parseString(documentStr).getAsJsonObject());
		Assertions.assertEquals("test-id", id);
	}

	@Test
	public void getById() throws Exception {
		// set up
		JsonObject answerDetailsObj = new JsonObject();
		answerDetailsObj.addProperty(Constants.NICKNAME_ATTRIBUTE, "nick");
		answerDetailsObj.addProperty("How are you?", "good!");
		// execute
		String id = operations.indexDocument(TEST_INDEX, answerDetailsObj, "test-id");

		refresh(TEST_INDEX);
		Method privateMethod = ElasticClientOperations.class.getDeclaredMethod("getById", String.class, String.class);
		privateMethod.setAccessible(true);

		String documentStr = (String) privateMethod.invoke(operations, TEST_INDEX, "test-id");

		// assert
		String expectedDocument = "{\"nickname\":\"nick\",\"How are you?\":\"good!\"}";
		assertEquals("answer details has to have the success response!",
				JsonParser.parseString(expectedDocument).getAsJsonObject(),
				JsonParser.parseString(documentStr).getAsJsonObject());
		Assertions.assertEquals("test-id", id);
	}

	private void indexDocuments(JsonArray initialQuestions) {
		for (int i = 0; i < initialQuestions.size(); i++) {
			try {
				operations.indexDocument(TEST_INDEX, initialQuestions.get(i).getAsJsonObject(), String.valueOf(i));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	@Test
	public void getAnswers_oneRelatedDocumentAndOneUnrelatedDocument() throws Exception {
		// set up
		String initialQuestionsStr = "[{\"nickname\":\"xxx\",\"name\":\"2020-11-15 21:20:17\",\"category\":\"introversion\",\"answers\":[{\"question\":\"Do you enjoy spending time alone?\",\"answer\":\"most of the time\"}]},{\"nickname\":\"xx\",\"name\":\"2020-11-15 21:18:57\",\"category\":\"introversion\",\"answers\":[{\"question\":\"Do you enjoy spending time alone?\",\"answer\":\"most of the time\"}]}]";
		JsonArray initialQuestions = JsonParser.parseString(initialQuestionsStr).getAsJsonArray();
		for (int i = 0; i < initialQuestions.size(); i++) {
			try {
				JsonObject documentObj = initialQuestions.get(i).getAsJsonObject();
				operations.indexDocument(TEST_ANSWER_INDEX, documentObj,
						documentObj.get("nickname").getAsString() + "_" + documentObj.get("category").getAsString());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		refresh(TEST_ANSWER_INDEX);

		// execute
		JsonArray answerDetails = operations.getAnswers("xxx", TEST_ANSWER_INDEX);

		// assert
		String expectedAnswerDetails = "[{\"nickname\":\"xxx\",\"name\":\"2020-11-15 21:20:17\",\"category\":\"introversion\",\"answers\":[{\"question\":\"Do you enjoy spending time alone?\",\"answer\":\"most of the time\"}]}]";
		assertEquals("expectedAnswerDetails must has one document",
				JsonParser.parseString(expectedAnswerDetails).getAsJsonArray(), answerDetails);
	}

	@Test
	public void getAnswers_twoDocument() throws Exception {
		// set up
		String initialQuestionsStr = "[{\"nickname\":\"xx\",\"name\":\"2020-11-15 21:20:17\",\"category\":\"introversion\",\"answers\":[{\"question\":\"Do you enjoy spending time alone?\",\"answer\":\"most of the time\"}]},{\"nickname\":\"xx\",\"name\":\"2020-11-15 21:18:57\",\"category\":\"lifestyle\",\"answers\":[{\"question\":\"Do you enjoy spending time alone?\",\"answer\":\"most of the time\"}]}]";
		JsonArray initialQuestions = JsonParser.parseString(initialQuestionsStr).getAsJsonArray();
		for (int i = 0; i < initialQuestions.size(); i++) {
			try {
				JsonObject documentObj = initialQuestions.get(i).getAsJsonObject();
				operations.indexDocument(TEST_ANSWER_INDEX, documentObj,
						documentObj.get("nickname").getAsString() + "_" + documentObj.get("category").getAsString());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		refresh(TEST_ANSWER_INDEX);

		// execute
		JsonArray answerDetails = operations.getAnswers("xx", TEST_ANSWER_INDEX);

		// assert
		String expectedAnswerDetails = "[{\"nickname\":\"xx\",\"name\":\"2020-11-15 21:20:17\",\"category\":\"introversion\",\"answers\":[{\"question\":\"Do you enjoy spending time alone?\",\"answer\":\"most of the time\"}]},{\"nickname\":\"xx\",\"name\":\"2020-11-15 21:18:57\",\"category\":\"lifestyle\",\"answers\":[{\"question\":\"Do you enjoy spending time alone?\",\"answer\":\"most of the time\"}]}]";
		assertEquals("expectedAnswerDetails must has two documents",
				JsonParser.parseString(expectedAnswerDetails).getAsJsonArray(), answerDetails);
	}

	
	public boolean indexAvailable(String indexName) {
		boolean indexExists = false;
		GetIndexRequest indexRequest = new GetIndexRequest(indexName);
		try {
			indexExists = testClient.indices().exists(indexRequest, RequestOptions.DEFAULT);
		} catch (Throwable e) {
			e.printStackTrace();
		}
		return indexExists;
	}

	public boolean deleteIndex(String index) {
		boolean isAcknowledged = false;
		DeleteIndexRequest request = new DeleteIndexRequest(index);
		AcknowledgedResponse deleteIndexResponse = null;
		try {
			deleteIndexResponse = testClient.indices().delete(request, RequestOptions.DEFAULT);
		} catch (IOException e) {
			e.printStackTrace();
		}
		isAcknowledged = deleteIndexResponse.isAcknowledged();
		return isAcknowledged;
	}

	public boolean createTemplate(String templateName, String templateSource) {
		PutIndexTemplateRequest request = new PutIndexTemplateRequest(templateName);
		request.source(templateSource, XContentType.JSON);
		AcknowledgedResponse response = null;
		try {
			response = testClient.indices().putTemplate(request, RequestOptions.DEFAULT);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return response.isAcknowledged();
	}

	public boolean createIndex(String index, String mapping, int shardCount) {
		CreateIndexRequest createIndexReq = new CreateIndexRequest(index);
		// add index mapping to request
		if (mapping != null) {
			createIndexReq.settings(Settings.builder().put("index.number_of_shards", shardCount));
			createIndexReq.mapping(mapping, XContentType.JSON);
		}
		// create index
		CreateIndexResponse createIndexResp = null;
		try {
			createIndexResp = testClient.indices().create(createIndexReq, RequestOptions.DEFAULT);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return createIndexResp.isAcknowledged();
	}

	public void refresh(String index) {
		RefreshRequest refreshRequest = new RefreshRequest(index);
		try {
			testClient.indices().refresh(refreshRequest, RequestOptions.DEFAULT);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public String getById(String indexName, String id) throws Exception {
		String doc = null;
		GetResponse response = testClient.get(new GetRequest().index(indexName).id(id), RequestOptions.DEFAULT);
		if (response.isExists() && !response.isSourceEmpty()) {
			doc = response.getSourceAsString();
		}
		return doc;
	}

}
