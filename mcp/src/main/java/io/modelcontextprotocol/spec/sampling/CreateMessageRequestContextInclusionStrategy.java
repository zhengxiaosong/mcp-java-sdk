package io.modelcontextprotocol.spec.sampling;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * 上下文包含策略。
 */
public enum CreateMessageRequestContextInclusionStrategy {

	NONE("none"), PARTIAL("partial"), FULL("full");

	private final String value;

	CreateMessageRequestContextInclusionStrategy(String value) {
		this.value = value;
	}

	@JsonValue
	public String getValue() {
		return value;
	}

	@JsonCreator
	public static CreateMessageRequestContextInclusionStrategy fromValue(String value) {
		for (CreateMessageRequestContextInclusionStrategy s : values()) {
			if (s.value.equalsIgnoreCase(value)) {
				return s;
			}
		}
		throw new IllegalArgumentException("Unknown value: " + value);
	}

}
