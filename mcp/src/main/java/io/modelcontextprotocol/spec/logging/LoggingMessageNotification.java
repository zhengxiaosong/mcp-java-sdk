package io.modelcontextprotocol.spec.logging;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;

/**
 * 日志消息通知。
 *
 * @param level 日志级别
 * @param logger 日志记录器
 * @param data 日志数据
 */
@JsonInclude(JsonInclude.Include.NON_ABSENT)
@JsonIgnoreProperties(ignoreUnknown = true)
public final class LoggingMessageNotification {

	@JsonProperty("level")
	private final LoggingLevel level;

	@JsonProperty("logger")
	private final String logger;

	@JsonProperty("data")
	private final String data;

	public LoggingMessageNotification(@JsonProperty("level") LoggingLevel level, @JsonProperty("logger") String logger,
			@JsonProperty("data") String data) {
		this.level = level;
		this.logger = logger;
		this.data = data;
	}

	public LoggingLevel getLevel() {
		return this.level;
	}

	public String getLogger() {
		return this.logger;
	}

	public String getData() {
		return this.data;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		LoggingMessageNotification that = (LoggingMessageNotification) o;
		return this.level == that.level && Objects.equals(this.logger, that.logger)
				&& Objects.equals(this.data, that.data);
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.level, this.logger, this.data);
	}

	@Override
	public String toString() {
		return "LoggingMessageNotification{" + "level=" + this.level + ", logger='" + this.logger + '\'' + ", data='"
				+ this.data + '\'' + '}';
	}

	public static Builder builder() {
		return new Builder();
	}

	public static class Builder {

		private LoggingLevel level = LoggingLevel.INFO;

		private String logger = "server";

		private String data;

		public Builder level(LoggingLevel level) {
			this.level = level;
			return this;
		}

		public Builder logger(String logger) {
			this.logger = logger;
			return this;
		}

		public Builder data(String data) {
			this.data = data;
			return this;
		}

		public LoggingMessageNotification build() {
			return new LoggingMessageNotification(this.level, this.logger, this.data);
		}

	}

}
