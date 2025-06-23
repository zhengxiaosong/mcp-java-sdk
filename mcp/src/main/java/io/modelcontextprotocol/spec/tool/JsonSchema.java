package io.modelcontextprotocol.spec.tool;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * JSON Schema对象。
 */
@JsonInclude(JsonInclude.Include.NON_ABSENT)
@JsonIgnoreProperties(ignoreUnknown = true)
public class JsonSchema {

	@JsonProperty("type")
	private final String type;

	@JsonProperty("properties")
	private final Map<String, Object> properties;

	@JsonProperty("required")
	private final List<String> required;

	@JsonProperty("additionalProperties")
	private final Boolean additionalProperties;

	@JsonProperty("$defs")
	private final Map<String, Object> defs;

	@JsonProperty("definitions")
	private final Map<String, Object> definitions;

	private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

	public JsonSchema(@JsonProperty("type") String type, @JsonProperty("properties") Map<String, Object> properties,
			@JsonProperty("required") List<String> required,
			@JsonProperty("additionalProperties") Boolean additionalProperties,
			@JsonProperty("$defs") Map<String, Object> defs,
			@JsonProperty("definitions") Map<String, Object> definitions) {
		this.type = type;
		this.properties = properties;
		this.required = required;
		this.additionalProperties = additionalProperties;
		this.defs = defs;
		this.definitions = definitions;
	}

	public static JsonSchema parseSchema(String schema) {
		try {
			return OBJECT_MAPPER.readValue(schema, JsonSchema.class);
		}
		catch (IOException e) {
			throw new IllegalArgumentException("Invalid schema: " + schema, e);
		}
	}

	public String getType() {
		return type;
	}

	public Map<String, Object> getProperties() {
		return properties;
	}

	public List<String> getRequired() {
		return required;
	}

	public Boolean getAdditionalProperties() {
		return additionalProperties;
	}

	public Map<String, Object> getDefs() {
		return defs;
	}

	public Map<String, Object> getDefinitions() {
		return definitions;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		JsonSchema that = (JsonSchema) o;
		return Objects.equals(type, that.type) && Objects.equals(properties, that.properties)
				&& Objects.equals(required, that.required)
				&& Objects.equals(additionalProperties, that.additionalProperties) && Objects.equals(defs, that.defs)
				&& Objects.equals(definitions, that.definitions);
	}

	@Override
	public int hashCode() {
		return Objects.hash(type, properties, required, additionalProperties, defs, definitions);
	}

	@Override
	public String toString() {
		return "JsonSchema{" + "type='" + type + '\'' + ", properties=" + properties + ", required=" + required
				+ ", additionalProperties=" + additionalProperties + ", defs=" + defs + ", definitions=" + definitions
				+ '}';
	}

}