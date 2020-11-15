package personalitytest;

/**
 * This class stores all constant values that are used in the project.
 * 
 * @author gizemabali
 *
 */
public class Constants {

	/**
	 * index name of the index that stores question details
	 */
	public static final String QUESTIONS_INDEX = "questions";

	/**
	 * index name of the index that stores answer details
	 */
	public static final String ANSWER_INDEX = "answer";

	/**
	 * field value for the responses.
	 */
	public static final String RESPONSE = "response";

	public static final String ANSWER = "answer";

	public static final String ANSWERS = "answers";

	public static final String OPTIONS = "options";

	public static final String TYPE = "type";

	public static final String QUESTION_TYPE = "question_type";

	public static final String QUESTION = "question";

	public static final String CATEGORY = "category";

	public static final String TOP_TAGS = "top_tags";

	public static final String CATEGORIES = "categories";

	public static final String NICKNAME_ATTRIBUTE = "nickname";
	
	public static final String ELASTIC_PORT = "ELASTIC_PORT";
	public static final String ELASTIC_HOSTNAME = "ELASTIC_HOSTNAME";
	public static final String LOCALHOST = "localhost";
	public static final String ANSWER_INDEX_ENV = "ANSWER_INDEX_ENV";
	public static final String QUESTION_INDEX_ENV = "QUESTION_INDEX_ENV";

	/**
	 * elastic index source field
	 */
	public static final String _SOURCE = "_source";

	/**
	 * date format for the answer information
	 */
	public static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";

	public static final String GMT_3 = "GMT+3";

	/**
	 * This class stores all paths of the apis.
	 * 
	 * @author gizemabali
	 *
	 */
	public static class Paths {

		public static final String GET_CATEGORIES_PATH = "/category";

		public static final String GET_QUESTION_DETAILS_PATH = "/category/{category}/questions";

		public static final String GET_ANSWERS_PATH = "/answers/{nickname}";

		public static final String SAVE_ANSWERS_PATH = "/answers/{category}";
	}

	/**
	 * This class stores error messages for logs and responses to the user.
	 * 
	 * @author gizemabali
	 *
	 */
	public static class ErrorMessages {
		public static final String COULD_NOT_SEARCH_IN_ELASTIC = "could not search in elastic!";

		public static final String UNEXPECTED_ERROR_OCCUR_PLEASE_TRY_AGAIN = "Unexpected error occur! Please try again!";

		public static final String ANSWERS_WERE_SAVED_BEFORE_FOR_THE_S_CATEGORY_BY_THE_NICKNAME_S_PLEASE_USE_A_DIFFERENT_NICKNAME = "Answers were saved before for the %s category by the nickname %s. Please use a different nickname!";

		public static final String ANSWERS_WERE_SAVED_FOR_THE_S_CATEGORY = "Answers were saved for the %s category!";

		public static final String GET_ANSWERS_ERROR = "getAnswersError";

		public static final String UNEXPECTED_ERROR_OCCUR = "Unexpected error occur!";

		public static final String GET_CATEGORIES_ERROR = "getCategoriesError";

		public static final String GET_QUESTION_DETAILS_ERROR = "getQuestionDetailsError";

		public static final String SAVE_ANSWERS_ERROR = "saveAnswersError";
	}
}
