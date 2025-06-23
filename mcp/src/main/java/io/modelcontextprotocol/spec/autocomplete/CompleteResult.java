package io.modelcontextprotocol.spec.autocomplete;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Objects;

/**
 * 补全结果。
 *
 * @param completions 补全项
 */
@JsonInclude(JsonInclude.Include.NON_ABSENT)
@JsonIgnoreProperties(ignoreUnknown = true)
public class CompleteResult {

	@JsonProperty("completions")
	private final List<CompleteResultCompleteCompletion> completions;

	public CompleteResult(@JsonProperty("completions") List<CompleteResultCompleteCompletion> completions) {
		this.completions = completions;
	}

	public List<CompleteResultCompleteCompletion> getCompletions() {
		return completions;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		CompleteResult that = (CompleteResult) o;
		return Objects.equals(completions, that.completions);
	}

	@Override
	public int hashCode() {
		return Objects.hash(completions);
	}

	@Override
	public String toString() {
		return "CompleteResult{" + "completions=" + completions + '}';
	}

}
