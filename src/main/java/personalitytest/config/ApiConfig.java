package personalitytest.config;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import personalitytest.Constants;

/**
 * This class is used to get env variables.
 * 
 * @author gizemabali
 *
 */
public class ApiConfig {

	public static final Logger logger = LogManager.getLogger(ApiConfig.class);

	/**
	 * This method returns name of the index that has questions and their details.
	 * 
	 * @return
	 */
	public static String getQuestionIndexName() {
		return getValueFromEnvironment(Constants.QUESTIONS_INDEX, Constants.QUESTION_INDEX_ENV);
	}

	/**
	 * This methed is used to return name of the index that is used to store answers of the users.
	 * 
	 * @return
	 */
	public static String getAnswerIndexName() {
		return getValueFromEnvironment(Constants.ANSWER_INDEX, Constants.ANSWER_INDEX_ENV);
	}

	/**
	 * This method is used to get host name of the elastic search.
	 * 
	 * @return
	 */
	public static String getElasticHostName() {
		return getValueFromEnvironment(Constants.LOCALHOST, Constants.ELASTIC_HOSTNAME);
	}

	/**
	 * This metohd is used to get port name of the elastic search.
	 * 
	 * @return
	 */
	public static int getElasticPort() {
		return getValueFromEnvironment(9200, Constants.ELASTIC_PORT);
	}

	/**
	 * This method is used to get string values of the environment variables.
	 * 
	 * @param defaultValue    it is the default value of the environement variable defined in the system.
	 * @param envVariableName it is the name of the environement variable.
	 * @return a string value of the variable.
	 */
	private static String getValueFromEnvironment(String defaultValue, String envVariableName) {
		String value = defaultValue;
		String envValue = System.getenv(envVariableName);
		if (envValue != null && !"".equals(envValue)) {
			value = envValue;
		}
		return value;
	}

	/**
	 * This method is used to get string values of the environment variables.
	 * 
	 * @param defaultValue    it is the default value of the environement variable defined in the system.
	 * @param envVariableName it is the name of the environement variable.
	 * @return a int value of the variable.
	 */
	private static int getValueFromEnvironment(int defaultValue, String envVariableName) {
		int value = defaultValue;
		String envValue = System.getenv(envVariableName);
		if (envValue != null && !"".equals(envValue)) {
			try {
				value = Integer.parseInt(envValue);
			} catch (Exception e) {
				logger.error("could not get value from environment", e);
			}
		}
		return value;
	}

}
