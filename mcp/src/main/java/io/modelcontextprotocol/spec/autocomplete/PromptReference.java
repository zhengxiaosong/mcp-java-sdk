package io.modelcontextprotocol.spec.autocomplete;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;

/**
 * 提示引用。
 *
 * @param name 提示名称
 */
@JsonInclude(JsonInclude.Include.NON_ABSENT)
@JsonIgnoreProperties(ignoreUnknown = true)
public class PromptReference implements CompleteReference {

	@JsonProperty("type")
	private final String type = "ref/prompt";

	@JsonProperty("name")
	private final String name;

	public PromptReference(@JsonProperty("name") String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	@Override
	public String getType() {
		return type;
	}

	@Override
	public String getIdentifier() {
		return name;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		PromptReference that = (PromptReference) o;
		return Objects.equals(type, that.type) && Objects.equals(name, that.name);
	}

	@Override
	public int hashCode() {
		return Objects.hash(type, name);
	}

	@Override
	public String toString() {
		return "PromptReference{" + "type='" + type + '\'' + ", name='" + name + '\'' + '}';
	}

}
