package personalitytest.pojos;

/**
 * This class is used for index operations for elastic index.
 * 
 * @author gizemabali
 *
 */
public class IfPositive {

	private String question;

	private String category;

	private QuestionType question_type;

	public String getQuestion() {
		return question;
	}

	public void setQuestion(String question) {
		this.question = question;
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public QuestionType getQuestion_type() {
		return question_type;
	}

	public void setQuestion_type(QuestionType question_type) {
		this.question_type = question_type;
	}

}
