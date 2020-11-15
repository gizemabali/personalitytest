package personalitytest.pojos;

import java.util.List;

import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

/**
 * This class is used for index operations for elastic index.
 * 
 * @author gizemabali
 *
 */
public class QuestionType {

	private String type;
	
	private List<String> options;
	
	private TestRange range;
	
	@Field(type = FieldType.Nested, includeInParent = true)
	private Condition condition;

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public List<String> getOptions() {
		return options;
	}

	public void setOptions(List<String> options) {
		this.options = options;
	}

	public TestRange getRange() {
		return range;
	}

	public void setRange(TestRange range) {
		this.range = range;
	}

	public Condition getCondition() {
		return condition;
	}

	public void setCondition(Condition condition) {
		this.condition = condition;
	}



}
