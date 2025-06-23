package io.modelcontextprotocol.spec.logging;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;

/**
 * 进度通知。
 *
 * @param message 进度消息
 * @param percent 百分比
 */
@JsonInclude(JsonInclude.Include.NON_ABSENT)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ProgressNotification {

	@JsonProperty("message")
	private final String message;

	@JsonProperty("percent")
	private final Double percent;

	public ProgressNotification(@JsonProperty("message") String message, @JsonProperty("percent") Double percent) {
		this.message = message;
		this.percent = percent;
	}

	public String getMessage() {
		return message;
	}

	public Double getPercent() {
		return percent;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		ProgressNotification that = (ProgressNotification) o;
		return Objects.equals(message, that.message) && Objects.equals(percent, that.percent);
	}

	@Override
	public int hashCode() {
		return Objects.hash(message, percent);
	}

	@Override
	public String toString() {
		return "ProgressNotification{" + "message='" + message + '\'' + ", percent=" + percent + '}';
	}

}
