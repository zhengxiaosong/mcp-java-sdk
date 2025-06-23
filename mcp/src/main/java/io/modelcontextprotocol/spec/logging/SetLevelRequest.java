package io.modelcontextprotocol.spec.logging;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;

/**
 * 设置日志级别请求。
 *
 * @param level 日志级别
 */
@JsonInclude(JsonInclude.Include.NON_ABSENT)
@JsonIgnoreProperties(ignoreUnknown = true)
public class SetLevelRequest {

	@JsonProperty("level")
	private final LoggingLevel level;

	public SetLevelRequest(@JsonProperty("level") LoggingLevel level) {
		this.level = level;
	}

	public LoggingLevel getLevel() {
		return level;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		SetLevelRequest that = (SetLevelRequest) o;
		return level == that.level;
	}

	@Override
	public int hashCode() {
		return Objects.hash(level);
	}

	@Override
	public String toString() {
		return "SetLevelRequest{" + "level=" + level + '}';
	}

}
