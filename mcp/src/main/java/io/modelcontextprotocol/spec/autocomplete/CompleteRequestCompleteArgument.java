package io.modelcontextprotocol.spec.autocomplete;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;

/**
 * 补全请求参数。
 *
 * @param name 参数名
 * @param value 参数值
 */
@JsonInclude(JsonInclude.Include.NON_ABSENT)
@JsonIgnoreProperties(ignoreUnknown = true)
public class CompleteRequestCompleteArgument {

	@JsonProperty("name")
	private final String name;

	@JsonProperty("value")
	private final String value;

	public CompleteRequestCompleteArgument(@JsonProperty("name") String name, @JsonProperty("value") String value) {
		this.name = name;
		this.value = value;
	}

	public String getName() {
		return name;
	}

	public String getValue() {
		return value;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		CompleteRequestCompleteArgument that = (CompleteRequestCompleteArgument) o;
		return Objects.equals(name, that.name) && Objects.equals(value, that.value);
	}

	@Override
	public int hashCode() {
		return Objects.hash(name, value);
	}

	@Override
	public String toString() {
		return "CompleteRequestCompleteArgument{" + "name='" + name + '\'' + ", value='" + value + '\'' + '}';
	}

}
