package io.modelcontextprotocol.spec.prompt;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;
import java.util.Objects;

/**
 * 获取提示请求。
 *
 * @param name 提示名称
 * @param arguments 参数
 */
@JsonInclude(JsonInclude.Include.NON_ABSENT)
@JsonIgnoreProperties(ignoreUnknown = true)
public class GetPromptRequest {

	@JsonProperty("name")
	private final String name;

	@JsonProperty("arguments")
	private final Map<String, Object> arguments;

	public GetPromptRequest(@JsonProperty("name") String name,
			@JsonProperty("arguments") Map<String, Object> arguments) {
		this.name = name;
		this.arguments = arguments;
	}

	public String getName() {
		return name;
	}

	public Map<String, Object> getArguments() {
		return arguments;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		GetPromptRequest that = (GetPromptRequest) o;
		return Objects.equals(name, that.name) && Objects.equals(arguments, that.arguments);
	}

	@Override
	public int hashCode() {
		return Objects.hash(name, arguments);
	}

	@Override
	public String toString() {
		return "GetPromptRequest{" + "name='" + name + '\'' + ", arguments=" + arguments + '}';
	}

}
