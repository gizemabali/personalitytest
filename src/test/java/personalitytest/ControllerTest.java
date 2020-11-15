package personalitytest;

import static org.junit.Assert.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.IOException;
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
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.EnvironmentVariables;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.elasticsearch.client.ClientConfiguration;
import org.springframework.data.elasticsearch.client.RestClients;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class ControllerTest {

	private static final String TEST_INDEX = "test-index";

	private static final String TEST_ANSWER_INDEX = "test-answer-index";

	private RestHighLevelClient testClient = null;

	private ElasticClientOperations operations;

	@ClassRule
	public final static EnvironmentVariables environmentVariables = new EnvironmentVariables();

	@Before
	public void setUp() throws UnknownHostException, InterruptedException {
		environmentVariables.set(Constants.QUESTION_INDEX_ENV, TEST_INDEX);
		environmentVariables.set(Constants.ANSWER_INDEX_ENV, TEST_ANSWER_INDEX);
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

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@Test
	public void getCategory() throws Exception {
		// set up
		String initialQuestionsStr = "[{\"question\":\"What is your gender?\",\"category\":\"hard_fact\",\"question_type\":{\"type\":\"single_choice\",\"options\":[\"male\",\"female\",\"other\"]}}]";
		JsonArray initialQuestions = JsonParser.parseString(initialQuestionsStr).getAsJsonArray();
		indexDocuments(initialQuestions);
		refresh(TEST_INDEX);

		// execute
		MvcResult response = mockMvc.perform(get("/category", 42L).contentType("application/json"))
				.andExpect(status().isOk()).andReturn();
		MockHttpServletResponse resp = response.getResponse();
		String responseStr = resp.getContentAsString();

		// assert
		String expectedCategories = "{\"categories\":[\"hard_fact\"]}";
		assertEquals("expectedCategories has to one category!",
				JsonParser.parseString(expectedCategories).getAsJsonObject(),
				JsonParser.parseString(responseStr).getAsJsonObject());
		assertEquals("must be ok reponse!", 200, response.getResponse().getStatus());

	}

	@Test
	public void getQuestionDetails() throws Exception {
		// set up
		String initialQuestionsStr = "[{\"question\":\"What is your gender?\",\"category\":\"hard_fact\",\"question_type\":{\"type\":\"single_choice\",\"options\":[\"male\",\"female\",\"other\"]}}]";
		JsonArray initialQuestions = JsonParser.parseString(initialQuestionsStr).getAsJsonArray();
		indexDocuments(initialQuestions);
		refresh(TEST_INDEX);

		// execute
		MvcResult response = mockMvc.perform(get("/category/hard_fact/questions", 42L).contentType("application/json"))
				.andExpect(status().isOk()).andReturn();
		MockHttpServletResponse resp = response.getResponse();
		String responseStr = resp.getContentAsString();

		// assert
		String expectedQuestionDetails = "[{\"question\":\"What is your gender?\",\"type\":\"single_choice\",\"options\":[\"male\",\"female\",\"other\"]}]";
		assertEquals("expectedCategories has to one category!",
				JsonParser.parseString(expectedQuestionDetails).getAsJsonArray(),
				JsonParser.parseString(responseStr).getAsJsonArray());
		assertEquals("must be ok reponse!", 200, response.getResponse().getStatus());

	}

	@Test
	public void saveAnswers() throws Exception {
		// set up
		String initialQuestionsStr = "[{\"question\":\"What is your gender?\",\"category\":\"hard_fact\",\"question_type\":{\"type\":\"single_choice\",\"options\":[\"male\",\"female\",\"other\"]}}]";
		JsonArray initialQuestions = JsonParser.parseString(initialQuestionsStr).getAsJsonArray();
		indexDocuments(initialQuestions);
		refresh(TEST_ANSWER_INDEX);

		JsonObject answerDetailsObj = new JsonObject();
		answerDetailsObj.addProperty(Constants.NICKNAME_ATTRIBUTE, "nick");
		answerDetailsObj.addProperty("How are you?", "good!");

		// execute

		MvcResult response = mockMvc
				.perform(MockMvcRequestBuilders.post("/postbody/hard_fact").content(answerDetailsObj.toString())
						.contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
				.andReturn();

		MockHttpServletResponse resp = response.getResponse();
		String responseStr = resp.getContentAsString();

		// assert
		String expectedResponse = "{\"response\":\"Answers were saved for the hard_fact category!\"}";
		assertEquals("expectedResponse has to be a success response!",
				JsonParser.parseString(expectedResponse).getAsJsonObject(),
				JsonParser.parseString(responseStr).getAsJsonObject());
		assertEquals("must be ok reponse!", 200, response.getResponse().getStatus());
	}
	
	@Test
	public void getAnswers() throws Exception {
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

		JsonObject answerDetailsObj = new JsonObject();
		answerDetailsObj.addProperty(Constants.NICKNAME_ATTRIBUTE, "nick");
		answerDetailsObj.addProperty("How are you?", "good!");

		// execute

		MvcResult response = mockMvc
				.perform(MockMvcRequestBuilders.post("/answers/xxx").content(answerDetailsObj.toString())
						.contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
				.andReturn();

		MockHttpServletResponse resp = response.getResponse();
		String responseStr = resp.getContentAsString();
		

		// assert
		String expectedResponse = "[{\"nickname\":\"xxx\",\"name\":\"2020-11-15 21:20:17\",\"category\":\"introversion\",\"answers\":[{\"question\":\"Do you enjoy spending time alone?\",\"answer\":\"most of the time\"}]}]";
		assertEquals("expectedResponse has to be a success response!",
				JsonParser.parseString(expectedResponse).getAsJsonArray(),
				JsonParser.parseString(responseStr).getAsJsonArray());
		assertEquals("must be ok reponse!", 200, response.getResponse().getStatus());
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
