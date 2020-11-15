package personalitytest.pojos;

import java.util.List;

/**
 * This class is used for index operations for elastic index.
 * 
 * @author gizemabali
 *
 */
public class Predicate {

	private List<String> exactEquals;

	public List<String> getExactEquals() {
		return exactEquals;
	}

	public void setExactEquals(List<String> exactEquals) {
		this.exactEquals = exactEquals;
	}
}
