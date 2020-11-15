package personalitytest.pojos;

import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;


/**
 * This class is used for index operations for elastic index.
 * 
 * @author gizemabali
 *
 */
@Document(indexName = "questions")
public class Question {

	private String question;

	private String category;

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public String getQuestion() {
		return question;
	}

	public void setQuestion(String question) {
		this.question = question;
	}

	public QuestionType getQuestion_type() {
		return question_type;
	}

	public void setQuestion_type(QuestionType question_type) {
		this.question_type = question_type;
	}

	@Field(type = FieldType.Nested, includeInParent = true)
	private QuestionType question_type;

}
