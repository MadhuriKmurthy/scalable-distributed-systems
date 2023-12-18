package project4;

/**
 * Class that represents an key value operation on the map.
 */
public class Operation {

	private String operationType;
	private String key;
	private String value;

	public Operation(String operationType, String key, String value) {
		this.operationType = operationType;
		this.key = key;
		this.value = value;
	}

	public String getOperationType() {
		return operationType;
	}

	public String getKey() {
		return key;
	}

	public String getValue() {
		return value;
	}

}
