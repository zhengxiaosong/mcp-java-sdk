package io.modelcontextprotocol.spec.autocomplete;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;

/**
 * 补全结果项。
 *
 * @param text 补全文本
 * @param score 置信分数
 */
@JsonInclude(JsonInclude.Include.NON_ABSENT)
@JsonIgnoreProperties(ignoreUnknown = true)
public class CompleteResultCompleteCompletion {

	@JsonProperty("text")
	private final String text;

	@JsonProperty("score")
	private final Double score;

	public CompleteResultCompleteCompletion(@JsonProperty("text") String text, @JsonProperty("score") Double score) {
		this.text = text;
		this.score = score;
	}

	public String getText() {
		return text;
	}

	public Double getScore() {
		return score;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		CompleteResultCompleteCompletion that = (CompleteResultCompleteCompletion) o;
		return Objects.equals(text, that.text) && Objects.equals(score, that.score);
	}

	@Override
	public int hashCode() {
		return Objects.hash(text, score);
	}

	@Override
	public String toString() {
		return "CompleteResultCompleteCompletion{" + "text='" + text + '\'' + ", score=" + score + '}';
	}

}
