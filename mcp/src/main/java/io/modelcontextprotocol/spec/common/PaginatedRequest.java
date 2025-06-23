package io.modelcontextprotocol.spec.common;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;

/**
 * 分页请求参数。
 *
 * @param cursor 分页游标
 */
@JsonInclude(JsonInclude.Include.NON_ABSENT)
@JsonIgnoreProperties(ignoreUnknown = true)
public class PaginatedRequest {

	@JsonProperty("cursor")
	private final String cursor;

	public PaginatedRequest(@JsonProperty("cursor") String cursor) {
		this.cursor = cursor;
	}

	public String getCursor() {
		return cursor;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		PaginatedRequest that = (PaginatedRequest) o;
		return Objects.equals(cursor, that.cursor);
	}

	@Override
	public int hashCode() {
		return Objects.hash(cursor);
	}

	@Override
	public String toString() {
		return "PaginatedRequest{" + "cursor='" + cursor + '\'' + '}';
	}

}
