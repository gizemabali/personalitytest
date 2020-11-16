package personalitytest.controllers;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import personalitytest.Constants;
import personalitytest.ElasticClientOperations;
import personalitytest.config.ApiConfig;

/**
 * This class is a controller class which handles requests from the clients.
 * 
 * @author gizemabali
 *
 */
@RestController
public class PersonalTestController {

	private static final Logger logger = LogManager.getLogger(ElasticClientOperations.class);

	/**
	 * This is a rest client of elastic to perform elastic requests.
	 */
	@Autowired
	RestHighLevelClient client;

	/**
	 * This api is used to handle requests which wants to get categories of questions from the elastic search. The
	 * search operations is done over question index {@value Constants.QUESTIONS_INDEX} that stores all questions.
	 * 
	 * @return a ResponseEntity object to the client.
	 */
	@CrossOrigin
	@RequestMapping(value = Constants.Paths.GET_CATEGORIES_PATH, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<String> getCategories() {
		try {
			return ResponseEntity.ok(
					ElasticClientOperations.getInstance().getCategories(ApiConfig.getQuestionIndexName()).toString());
		} catch (Exception e) {
			logger.error(Constants.ErrorMessages.GET_CATEGORIES_ERROR, e);
			return sendErrorResponse();
		}
	}

	/**
	 * This api is used to get questions and their options which are related to the given category. It searchs for
	 * question details in question index {@value Constants.QUESTIONS_INDEX}.
	 * 
	 * @param category this information indicates question category to group all questions.
	 * @return a ResponseEntity object to the client.
	 */
	@CrossOrigin
	@RequestMapping(value = Constants.Paths.GET_QUESTION_DETAILS_PATH, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<String> getQuestionDetails(@PathVariable String category) {
		try {
			return ResponseEntity.ok(ElasticClientOperations.getInstance()
					.getQuestionDetails(category, ApiConfig.getQuestionIndexName()).toString());
		} catch (Exception e) {
			logger.error(Constants.ErrorMessages.GET_QUESTION_DETAILS_ERROR, e);
			return sendErrorResponse();
		}
	}

	/**
	 * This api is used to save answers sent by the user which is related to given category. It stores all given answers
	 * to answer index {@value Constants.ANSWER_INDEX}.
	 * 
	 * @param category      this information indicates question category to group all questions.
	 * @param answerDetails it is an object that stores all answers sent for the given category and the nickname of the
	 *                      user.
	 * @return a ResponseEntity object to the client.
	 */
	@CrossOrigin
	@PostMapping(Constants.Paths.SAVE_ANSWERS_PATH)
	public ResponseEntity<String> saveAnswers(@PathVariable String category, @RequestBody String answerDetails) {
		try {
			JsonObject answerDetailsObj = JsonParser.parseString(answerDetails).getAsJsonObject();
			String string = ElasticClientOperations.getInstance()
					.saveAnswerDetails(category, answerDetailsObj, ApiConfig.getAnswerIndexName()).toString();
			return ResponseEntity.ok(string);
		} catch (Exception e) {
			logger.error(Constants.ErrorMessages.SAVE_ANSWERS_ERROR, e);
			return sendErrorResponse();
		}
	}

	/**
	 * This api is used to get answers sent by the given nickname of the user. It searchs for answers in answer index
	 * {@value Constants.ANSWER_INDEX} of elasticsearch.
	 * 
	 * @param nickname it is the nickname of the user which sends answers. nickname attribute must be unique and it is
	 *                 also used to form index id.
	 * @return a ResponseEntity object to the client.
	 */
	@CrossOrigin
	@RequestMapping(value = Constants.Paths.GET_ANSWERS_PATH, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<String> getAnswers(@PathVariable String nickname) {
		try {
			return ResponseEntity.ok(ElasticClientOperations.getInstance()
					.getAnswers(nickname, ApiConfig.getAnswerIndexName()).toString());
		} catch (Exception e) {
			logger.error(Constants.ErrorMessages.GET_ANSWERS_ERROR, e);
			return sendErrorResponse();
		}
	}

	/**
	 * This method creates and unexpected error occur response to the user when an expected error occur!
	 * <ul>
	 * <li><code>{"response":{@value Constants.ErrorMessages.UNEXPECTED_ERROR_OCCUR}}</code></li>
	 * </ul>
	 * 
	 * @return a ResponseEntity object
	 */
	private ResponseEntity<String> sendErrorResponse() {
		JsonObject responseObj = new JsonObject();
		responseObj.addProperty(Constants.RESPONSE, Constants.ErrorMessages.UNEXPECTED_ERROR_OCCUR);
		return ResponseEntity.status(500).body(responseObj.toString());
	}

}
