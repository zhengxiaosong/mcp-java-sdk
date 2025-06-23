package io.modelcontextprotocol.spec.logging;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 日志级别。
 */
public enum LoggingLevel {

	/**
	 * 调试。
	 */
	@JsonProperty("debug")
	DEBUG(0),
	/**
	 * 信息。
	 */
	@JsonProperty("info")
	INFO(1),
	/**
	 * 注意。
	 */
	@JsonProperty("notice")
	NOTICE(2),
	/**
	 * 警告。
	 */
	@JsonProperty("warning")
	WARNING(3),
	/**
	 * 错误。
	 */
	@JsonProperty("error")
	ERROR(4),
	/**
	 * 严重。
	 */
	@JsonProperty("critical")
	CRITICAL(5),
	/**
	 * 警报。
	 */
	@JsonProperty("alert")
	ALERT(6),
	/**
	 * 紧急。
	 */
	@JsonProperty("emergency")
	EMERGENCY(7);

	private final int level;

	LoggingLevel(int level) {
		this.level = level;
	}

	public int getLevel() {
		return this.level;
	}

}
