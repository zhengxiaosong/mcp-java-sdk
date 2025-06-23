package io.modelcontextprotocol.spec.autocomplete;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;

/**
 * 补全请求。
 *
 * @param ref 引用
 * @param argument 参数
 */
@JsonInclude(JsonInclude.Include.NON_ABSENT)
@JsonIgnoreProperties(ignoreUnknown = true)
public class CompleteRequest {

	@JsonProperty("ref")
	private final CompleteReference ref;

	@JsonProperty("argument")
	private final CompleteRequestCompleteArgument argument;

	public CompleteRequest(@JsonProperty("ref") CompleteReference ref,
			@JsonProperty("argument") CompleteRequestCompleteArgument argument) {
		this.ref = ref;
		this.argument = argument;
	}

	public CompleteReference getRef() {
		return ref;
	}

	public CompleteRequestCompleteArgument getArgument() {
		return argument;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		CompleteRequest that = (CompleteRequest) o;
		return Objects.equals(ref, that.ref) && Objects.equals(argument, that.argument);
	}

	@Override
	public int hashCode() {
		return Objects.hash(ref, argument);
	}

	@Override
	public String toString() {
		return "CompleteRequest{" + "ref=" + ref + ", argument=" + argument + '}';
	}

}
