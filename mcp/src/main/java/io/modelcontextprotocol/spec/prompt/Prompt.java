package io.modelcontextprotocol.spec.prompt;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Objects;

/**
 * 服务器提供的提示或提示模板。
 *
 * @param name 提示名称
 * @param description 描述
 * @param arguments 参数列表
 */
@JsonInclude(JsonInclude.Include.NON_ABSENT)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Prompt {

	@JsonProperty("name")
	private final String name;

	@JsonProperty("description")
	private final String description;

	@JsonProperty("arguments")
	private final List<PromptArgument> arguments;

	public Prompt(@JsonProperty("name") String name, @JsonProperty("description") String description,
			@JsonProperty("arguments") List<PromptArgument> arguments) {
		this.name = name;
		this.description = description;
		this.arguments = arguments;
	}

	public String getName() {
		return name;
	}

	public String getDescription() {
		return description;
	}

	public List<PromptArgument> getArguments() {
		return arguments;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		Prompt prompt = (Prompt) o;
		return Objects.equals(name, prompt.name) && Objects.equals(description, prompt.description)
				&& Objects.equals(arguments, prompt.arguments);
	}

	@Override
	public int hashCode() {
		return Objects.hash(name, description, arguments);
	}

	@Override
	public String toString() {
		return "Prompt{" + "name='" + name + '\'' + ", description='" + description + '\'' + ", arguments=" + arguments
				+ '}';
	}

}
