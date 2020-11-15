package personalitytest.config;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import personalitytest.Constants;

public class ApiConfig {
	
	public static final Logger logger = LogManager.getLogger(ApiConfig.class);

	public static String getQuestionIndexName() {
		return getValueFromEnvironment(Constants.QUESTIONS_INDEX, Constants.QUESTION_INDEX_ENV);
	}

	public static String getAnswerIndexName() {
		return getValueFromEnvironment(Constants.ANSWER_INDEX, Constants.ANSWER_INDEX_ENV);
	}

	public static String getElasticHostName() {
		return getValueFromEnvironment(Constants.LOCALHOST, Constants.ELASTIC_HOSTNAME);
	}

	public static int getElasticPort() {
		return getValueFromEnvironment(9200, Constants.ELASTIC_PORT);
	}

	private static String getValueFromEnvironment(String defaultValue, String envVariableName) {
		String value = defaultValue;
		String envValue = System.getenv(envVariableName);
		if (envValue != null && !"".equals(envValue)) {
			value = envValue;
		}
		return value;
	}

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
