package personalitytest;

import static org.junit.Assert.assertEquals;

import java.lang.reflect.Method;

import org.elasticsearch.client.RestHighLevelClient;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.mockito.Mockito;
import org.springframework.data.elasticsearch.client.ClientConfiguration;
import org.springframework.data.elasticsearch.client.RestClients;
import org.springframework.http.ResponseEntity;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import personalitytest.config.ApiConfig;

public class ElasticClientOperationsTest {

	private static final String ELASTIC_HOST = ApiConfig.getElasticHostName() + ":" + ApiConfig.getElasticPort();

	private static final String TEST_INDEX = "test-index";

	private static final String TEST_ANSWER_INDEX = "test-answer-index";

	private RestHighLevelClient testClient = null;

	private ElasticClientOperations operations;

	private ClientUtils clientUtils;

	@Before
	public void setUp() throws Exception {
		ClientConfiguration clientConfiguration = ClientConfiguration.builder().connectedTo(ELASTIC_HOST).build();
		testClient = RestClients.create(clientConfiguration).rest();
		operations = Mockito.mock(ElasticClientOperations.class);
		Mockito.doCallRealMethod().when(operations).getCategories(Mockito.anyString());
		Mockito.doCallRealMethod().when(operations).setClient(Mockito.any(RestHighLevelClient.class));
		Mockito.doCallRealMethod().when(operations).getQuestionDetails(Mockito.anyString(), Mockito.anyString());
		Mockito.doCallRealMethod().when(operations).getAnswers(Mockito.anyString(), Mockito.anyString());
		Mockito.doCallRealMethod().when(operations).saveAnswerDetails(Mockito.anyString(), Mockito.any(),
				Mockito.anyString());
		Mockito.doCallRealMethod().when(operations).indexDocument(Mockito.anyString(), Mockito.any(),
				Mockito.anyString());
		Mockito.doCallRealMethod().when(operations).validateAndCreateAnswerDocument(Mockito.any(), Mockito.any(),
				Mockito.any());

		Mockito.when(operations.getCurrentDate()).thenReturn("2020-11-15 23:40:57");
		operations.setClient(testClient);
		clientUtils = new ClientUtils(testClient);

		if (clientUtils.indexAvailable(TEST_INDEX)) {
			clientUtils.deleteIndex(TEST_INDEX);
		}
		if (clientUtils.indexAvailable(TEST_ANSWER_INDEX)) {
			clientUtils.deleteIndex(TEST_ANSWER_INDEX);
		}
		String textIndexTemplate = "{\"index_patterns\":[\"test-index*\"],\"settings\":{},\"mappings\":{\"_source\":{\"enabled\":true},\"properties\":{\"question\":{\"type\":\"keyword\"},\"question_type\":{\"type\":\"nested\",\"properties\":{\"options\":{\"type\":\"keyword\"},\"type\":{\"type\":\"keyword\"}}},\"category\":{\"type\":\"keyword\"}}},\"aliases\":{}}";
		clientUtils.createTemplate("template-text-index", textIndexTemplate);
		Thread.sleep(200);
		clientUtils.createIndex(TEST_INDEX, null, 1);

		String textAnswerIndexTemplate = "{\"index_patterns\":[\"answer*\"],\"settings\":{\"number_of_shards\":1},\"mappings\":{\"_source\":{\"enabled\":true},\"properties\":{\"nickname\":{\"type\":\"keyword\"},\"date\":{\"type\":\"date\",\"format\":\"yyyy-MM-dd HH:mm:ss\"},\"answers\":{\"type\":\"nested\",\"properties\":{\"question\":{\"type\":\"keyword\"},\"answer\":{\"type\":\"keyword\"}}}}}}";
		clientUtils.createTemplate("template-test-answer-index", textAnswerIndexTemplate);
		Thread.sleep(200);
		clientUtils.createIndex(TEST_ANSWER_INDEX, null, 1);
	}

	@After
	public void tearDown() {

	}

	@Test
	public void getCategories_oneCategory() throws Exception {
		// set up
		String initialQuestionsStr = "[{\"question\":\"What is your gender?\",\"category\":\"hard_fact\",\"question_type\":{\"type\":\"single_choice\",\"options\":[\"male\",\"female\",\"other\"]}}]";
		JsonArray initialQuestions = JsonParser.parseString(initialQuestionsStr).getAsJsonArray();
		indexDocuments(initialQuestions);
		clientUtils.refresh(TEST_INDEX);

		// execute
		JsonObject categories = operations.getCategories(TEST_INDEX);

		// assert
		String expectedCategories = "{\"categories\":[\"hard_fact\"]}";
		assertEquals("categories must has a hard_fact value",
				JsonParser.parseString(expectedCategories).getAsJsonObject(), categories);
	}

	@Test
	public void getCategories_twoCategory() throws Exception {
		// set up
		String initialQuestionsStr = "[{\"question\":\"What is your gender?\",\"category\":\"hard_fact\",\"question_type\":{\"type\":\"single_choice\",\"options\":[\"male\",\"female\",\"other\"]}},{\"question\":\"What is your marital status?\",\"category\":\"passion\",\"question_type\":{\"type\":\"single_choice\",\"options\":[\"never married\",\"separated\",\"divorced\",\"widowed\"]}}]";
		JsonArray initialQuestions = JsonParser.parseString(initialQuestionsStr).getAsJsonArray();
		indexDocuments(initialQuestions);
		clientUtils.refresh(TEST_INDEX);

		// execute
		JsonObject categories = operations.getCategories(TEST_INDEX);

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
		clientUtils.refresh(TEST_INDEX);

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
		clientUtils.refresh(TEST_INDEX);

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
		clientUtils.refresh(TEST_INDEX);

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
		ResponseEntity<String> response = operations.saveAnswerDetails("lifestyle", answerDetailsObj, TEST_INDEX);

		String documentStr = clientUtils.getById(TEST_INDEX, "nick_lifestyle");
		// assert
		String expectedDocument = "{\"nickname\":\"nick\",\"date\":\"2020-11-15 23:40:57\",\"category\":\"lifestyle\",\"answers\":[{\"question\":\"How are you?\",\"answer\":\"good!\"}]}";

		assertEquals("expectedDocument must has fields!", JsonParser.parseString(expectedDocument).getAsJsonObject(),
				JsonParser.parseString(documentStr).getAsJsonObject());

		String expectedAnswerTetails = "{\"response\":\"Answers were saved for the lifestyle category!\"}";
		assertEquals("response object has to have the success response!",
				JsonParser.parseString(expectedAnswerTetails).getAsJsonObject(),
				JsonParser.parseString(response.getBody()).getAsJsonObject());
		assertEquals("response must have an ok response!", 200, response.getStatusCodeValue());
	}

	@Test
	public void saveAnswerDetails_noNickName() throws Exception {
		// set up
		JsonObject answerDetailsObj = new JsonObject();
		answerDetailsObj.addProperty("How are you?", "good!");
		// execute
		ResponseEntity<String> response = operations.saveAnswerDetails("lifestyle", answerDetailsObj, TEST_INDEX);
		// assert
		String expectedAnswerTetails = "{\"response\":\"Your answers could not be saved. Please do not enter an empty nickname!\"}";
		assertEquals("response object has to have the failure response!",
				JsonParser.parseString(expectedAnswerTetails).getAsJsonObject(),
				JsonParser.parseString(response.getBody()).getAsJsonObject());
		assertEquals("response must have a 422 response!", 422, response.getStatusCodeValue());
	}

	@Test
	public void saveAnswerDetails_emptyAnswer() throws Exception {
		// set up
		JsonObject answerDetailsObj = new JsonObject();
		answerDetailsObj.addProperty(Constants.NICKNAME_ATTRIBUTE, "nick");
		answerDetailsObj.addProperty("How are you?", "");
		// execute
		ResponseEntity<String> response = operations.saveAnswerDetails("lifestyle", answerDetailsObj, TEST_INDEX);
		// assert
		String expectedAnswerTetails = "{\"response\":\"Your answers could not be saved. Please be sure that you answered all questions!\"}";
		assertEquals("response object has to have the failure response!",
				JsonParser.parseString(expectedAnswerTetails).getAsJsonObject(),
				JsonParser.parseString(response.getBody()).getAsJsonObject());
		assertEquals("response must have a 422 response!", 422, response.getStatusCodeValue());
	}

	@Test
	public void saveAnswerDetails_searchError() throws Exception {
		// set up
		JsonObject answerDetailsObj = new JsonObject();
		answerDetailsObj.addProperty(Constants.NICKNAME_ATTRIBUTE, "nick");
		answerDetailsObj.addProperty("How are you?", "fine");
		// execute
		ClientConfiguration clientConfiguration = ClientConfiguration.builder().connectedTo("wrong_host:9200").build();
		RestHighLevelClient testClient = RestClients.create(clientConfiguration).rest();
		operations.setClient(testClient);
		ResponseEntity<String> response = operations.saveAnswerDetails("lifestyle", answerDetailsObj, TEST_INDEX);
		// assert
		String expectedAnswerTetails = "{\"response\":\"Unexpected error occur! Please try again!\"}";
		assertEquals("response object has to have the failure response!",
				JsonParser.parseString(expectedAnswerTetails).getAsJsonObject(),
				JsonParser.parseString(response.getBody()).getAsJsonObject());
		assertEquals("response must have a 500 response!", 500, response.getStatusCodeValue());
	}

	@Test
	public void saveAnswerDetails_secondSaveAttemptFailure() throws Exception {
		// set up
		String initialQuestionsStr = "{\"nickname\":\"nick\",\"date\":\"2020-11-15 23:40:57\",\"category\":\"lifestyle\",\"answers\":[{\"question\":\"How are you?\",\"answer\":\"good!\"}]}";
		operations.indexDocument(TEST_INDEX, JsonParser.parseString(initialQuestionsStr).getAsJsonObject(),
				"nick_lifestyle");
		clientUtils.refresh(TEST_INDEX);

		JsonObject answerDetailsObj = new JsonObject();
		answerDetailsObj.addProperty(Constants.NICKNAME_ATTRIBUTE, "nick");
		answerDetailsObj.addProperty("How are you?", "good!");
		// execute
		ResponseEntity<String> response = operations.saveAnswerDetails("lifestyle", answerDetailsObj, TEST_INDEX);

		// assert
		String expectedAnswerTetails = "{\"response\":\"Answers were saved before for the lifestyle category by the nickname nick. Please use a different nickname!\"}";
		assertEquals("response object has to have the success response!",
				JsonParser.parseString(expectedAnswerTetails).getAsJsonObject(),
				JsonParser.parseString(response.getBody()).getAsJsonObject());
		assertEquals("response must have a 400 response!", 400, response.getStatusCodeValue());
	}

	@Test
	public void indexDocument() throws Exception {
		// set up
		JsonObject answerDetailsObj = new JsonObject();
		answerDetailsObj.addProperty(Constants.NICKNAME_ATTRIBUTE, "nick");
		answerDetailsObj.addProperty("How are you?", "good!");
		// execute
		String id = operations.indexDocument(TEST_INDEX, answerDetailsObj, "test-id");

		clientUtils.refresh(TEST_INDEX);
		String documentStr = clientUtils.getById(TEST_INDEX, "test-id");

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

		clientUtils.refresh(TEST_INDEX);
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
			} catch (Exception e) {
				System.err.println("could not index data");
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
			} catch (Exception e) {
				System.err.println("could not index data");
			}
		}
		clientUtils.refresh(TEST_ANSWER_INDEX);

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
			} catch (Exception e) {
				System.err.println("could not index data");
			}
		}
		clientUtils.refresh(TEST_ANSWER_INDEX);

		// execute
		JsonArray answerDetails = operations.getAnswers("xx", TEST_ANSWER_INDEX);

		// assert
		String expectedAnswerDetails = "[{\"nickname\":\"xx\",\"name\":\"2020-11-15 21:20:17\",\"category\":\"introversion\",\"answers\":[{\"question\":\"Do you enjoy spending time alone?\",\"answer\":\"most of the time\"}]},{\"nickname\":\"xx\",\"name\":\"2020-11-15 21:18:57\",\"category\":\"lifestyle\",\"answers\":[{\"question\":\"Do you enjoy spending time alone?\",\"answer\":\"most of the time\"}]}]";
		assertEquals("expectedAnswerDetails must has two documents",
				JsonParser.parseString(expectedAnswerDetails).getAsJsonArray(), answerDetails);
	}

}
