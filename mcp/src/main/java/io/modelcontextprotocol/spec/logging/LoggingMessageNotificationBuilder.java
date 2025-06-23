package io.modelcontextprotocol.spec.logging;

/**
 * LoggingMessageNotification 构建器。
 */
public class LoggingMessageNotificationBuilder {

	private LoggingLevel level = LoggingLevel.INFO;

	private String logger = "server";

	private String data;

	public LoggingMessageNotificationBuilder level(LoggingLevel level) {
		this.level = level;
		return this;
	}

	public LoggingMessageNotificationBuilder logger(String logger) {
		this.logger = logger;
		return this;
	}

	public LoggingMessageNotificationBuilder data(String data) {
		this.data = data;
		return this;
	}

	public LoggingMessageNotification build() {
		return new LoggingMessageNotification(level, logger, data);
	}

}
