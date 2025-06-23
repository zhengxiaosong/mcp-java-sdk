package io.modelcontextprotocol.spec.common;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;

/**
 * 分页响应结果。
 *
 * @param nextCursor 下一页游标
 */
@JsonInclude(JsonInclude.Include.NON_ABSENT)
@JsonIgnoreProperties(ignoreUnknown = true)
public class PaginatedResult {

	@JsonProperty("nextCursor")
	private final String nextCursor;

	public PaginatedResult(@JsonProperty("nextCursor") String nextCursor) {
		this.nextCursor = nextCursor;
	}

	public String getNextCursor() {
		return nextCursor;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		PaginatedResult that = (PaginatedResult) o;
		return Objects.equals(nextCursor, that.nextCursor);
	}

	@Override
	public int hashCode() {
		return Objects.hash(nextCursor);
	}

	@Override
	public String toString() {
		return "PaginatedResult{" + "nextCursor='" + nextCursor + '\'' + '}';
	}

}
