package io.modelcontextprotocol.spec.prompt;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;

/**
 * 提示参数。
 *
 * @param name 参数名
 * @param description 参数描述
 * @param required 是否必填
 */
@JsonInclude(JsonInclude.Include.NON_ABSENT)
@JsonIgnoreProperties(ignoreUnknown = true)
public class PromptArgument {

	@JsonProperty("name")
	private final String name;

	@JsonProperty("description")
	private final String description;

	@JsonProperty("required")
	private final Boolean required;

	public PromptArgument(@JsonProperty("name") String name, @JsonProperty("description") String description,
			@JsonProperty("required") Boolean required) {
		this.name = name;
		this.description = description;
		this.required = required;
	}

	public String getName() {
		return name;
	}

	public String getDescription() {
		return description;
	}

	public Boolean getRequired() {
		return required;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		PromptArgument that = (PromptArgument) o;
		return Objects.equals(name, that.name) && Objects.equals(description, that.description)
				&& Objects.equals(required, that.required);
	}

	@Override
	public int hashCode() {
		return Objects.hash(name, description, required);
	}

	@Override
	public String toString() {
		return "PromptArgument{" + "name='" + name + '\'' + ", description='" + description + '\'' + ", required="
				+ required + '}';
	}

}
