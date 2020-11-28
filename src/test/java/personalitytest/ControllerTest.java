package personalitytest;

import static org.junit.Assert.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.elasticsearch.client.RestHighLevelClient;
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
	
	private ClientUtils clientUtils;

	@ClassRule
	public final static EnvironmentVariables environmentVariables = new EnvironmentVariables();

	@Before
	public void setUp() throws Exception {
		environmentVariables.set(Constants.QUESTION_INDEX_ENV, TEST_INDEX);
		environmentVariables.set(Constants.ANSWER_INDEX_ENV, TEST_ANSWER_INDEX);
		ClientConfiguration clientConfiguration = ClientConfiguration.builder().connectedTo("localhost:9200").build();
		testClient = RestClients.create(clientConfiguration).rest();
		operations = Mockito.mock(ElasticClientOperations.class);
		Mockito.doCallRealMethod().when(operations).getCategories(Mockito.anyString());
		Mockito.doCallRealMethod().when(operations).setClient(Mockito.any(RestHighLevelClient.class));
		Mockito.doCallRealMethod().when(operations).getQuestionDetails(Mockito.anyString(), Mockito.anyString());
		Mockito.doCallRealMethod().when(operations).getAnswers(Mockito.anyString(), Mockito.anyString());
		Mockito.doCallRealMethod().when(operations).saveAnswerDetails(Mockito.anyString(), Mockito.any(), Mockito.anyString());
		Mockito.doCallRealMethod().when(operations).indexDocument(Mockito.anyString(), Mockito.any(), Mockito.anyString());
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

	@Autowired
	private MockMvc mockMvc;

	@Test
	public void getCategory() throws Exception {
		// set up
		String initialQuestionsStr = "[{\"question\":\"What is your gender?\",\"category\":\"hard_fact\",\"question_type\":{\"type\":\"single_choice\",\"options\":[\"male\",\"female\",\"other\"]}}]";
		JsonArray initialQuestions = JsonParser.parseString(initialQuestionsStr).getAsJsonArray();
		indexDocuments(initialQuestions);
		clientUtils.refresh(TEST_INDEX);

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
		clientUtils.refresh(TEST_INDEX);

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
		clientUtils.refresh(TEST_ANSWER_INDEX);

		JsonObject answerDetailsObj = new JsonObject();
		answerDetailsObj.addProperty(Constants.NICKNAME_ATTRIBUTE, "nick");
		answerDetailsObj.addProperty("How are you?", "good!");

		// execute

		MvcResult response = mockMvc
				.perform(MockMvcRequestBuilders.post("/answers/save/hard_fact").content(answerDetailsObj.toString())
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
			} catch (Exception e) {
				System.err.println("could not index data");
			}
		}
		clientUtils.refresh(TEST_ANSWER_INDEX);

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
			} catch (Exception e) {
				System.err.println("could not index data");
			}
		}
	}


}
