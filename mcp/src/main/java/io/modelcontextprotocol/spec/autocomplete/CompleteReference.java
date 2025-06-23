package io.modelcontextprotocol.spec.autocomplete;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;

/**
 * 补全引用。
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.DEDUCTION, include = As.PROPERTY)
@JsonSubTypes({ @JsonSubTypes.Type(value = PromptReference.class),
		@JsonSubTypes.Type(value = ResourceReference.class) })
public interface CompleteReference {

	String getType();

	String getIdentifier();

}
