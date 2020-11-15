package personalitytest.pojos;

/**
 * This class is used for index operations for elastic index.
 * 
 * @author gizemabali
 *
 */
public class Condition {

	private IfPositive if_positive;

	private Predicate predicate;

	public Predicate getPredicate() {
		return predicate;
	}

	public void setPredicate(Predicate predicate) {
		this.predicate = predicate;
	}

	public IfPositive getIf_positive() {
		return if_positive;
	}

	public void setIf_positive(IfPositive if_positive) {
		this.if_positive = if_positive;
	}

}
