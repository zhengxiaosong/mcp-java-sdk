package io.modelcontextprotocol.spec.tool;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;

/**
 * 服务器提供的工具。
 *
 * @param name 工具名
 * @param description 工具描述
 * @param inputSchema 输入参数的JSON Schema
 */
@JsonInclude(JsonInclude.Include.NON_ABSENT)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Tool {

	@JsonProperty("name")
	private final String name;

	@JsonProperty("description")
	private final String description;

	@JsonProperty("inputSchema")
	private final JsonSchema inputSchema;

	public Tool(@JsonProperty("name") String name, @JsonProperty("description") String description,
			@JsonProperty("inputSchema") JsonSchema inputSchema) {
		this.name = name;
		this.description = description;
		this.inputSchema = inputSchema;
	}

	public Tool(String name, String description, String schema) {
		this(name, description, JsonSchema.parseSchema(schema));
	}

	public String getName() {
		return name;
	}

	public String getDescription() {
		return description;
	}

	public JsonSchema getInputSchema() {
		return inputSchema;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		Tool tool = (Tool) o;
		return Objects.equals(name, tool.name) && Objects.equals(description, tool.description)
				&& Objects.equals(inputSchema, tool.inputSchema);
	}

	@Override
	public int hashCode() {
		return Objects.hash(name, description, inputSchema);
	}

	@Override
	public String toString() {
		return "Tool{" + "name='" + name + '\'' + ", description='" + description + '\'' + ", inputSchema="
				+ inputSchema + '}';
	}

}
