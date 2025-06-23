package io.modelcontextprotocol.spec.tool;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.modelcontextprotocol.spec.common.Request;
import java.io.IOException;
import java.util.Map;
import java.util.Objects;

/**
 * 工具调用请求。
 *
 * @param name 工具名
 * @param arguments 参数
 */
@JsonInclude(JsonInclude.Include.NON_ABSENT)
@JsonIgnoreProperties(ignoreUnknown = true)
public final class CallToolRequest implements Request {

	@JsonProperty("name")
	private final String name;

	@JsonProperty("arguments")
	private final Map<String, Object> arguments;

	private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

	public CallToolRequest(@JsonProperty("name") String name,
			@JsonProperty("arguments") Map<String, Object> arguments) {
		this.name = name;
		this.arguments = arguments;
	}

	public CallToolRequest(String name, String jsonArguments) {
		this(name, parseJsonArguments(jsonArguments));
	}

	private static Map<String, Object> parseJsonArguments(String jsonArguments) {
		try {
			return OBJECT_MAPPER.readValue(jsonArguments,
					OBJECT_MAPPER.getTypeFactory().constructMapType(Map.class, String.class, Object.class));
		}
		catch (IOException e) {
			throw new IllegalArgumentException("Invalid arguments: " + jsonArguments, e);
		}
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
		CallToolRequest that = (CallToolRequest) o;
		return Objects.equals(name, that.name) && Objects.equals(arguments, that.arguments);
	}

	@Override
	public int hashCode() {
		return Objects.hash(name, arguments);
	}

	@Override
	public String toString() {
		return "CallToolRequest{" + "name='" + name + '\'' + ", arguments=" + arguments + '}';
	}

}
