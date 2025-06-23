package io.modelcontextprotocol.spec.prompt;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Objects;

/**
 * prompts/list请求的响应。
 *
 * @param prompts 提示列表
 * @param nextCursor 分页游标
 */
@JsonInclude(JsonInclude.Include.NON_ABSENT)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ListPromptsResult {

	@JsonProperty("prompts")
	private final List<Prompt> prompts;

	@JsonProperty("nextCursor")
	private final String nextCursor;

	public ListPromptsResult(@JsonProperty("prompts") List<Prompt> prompts,
			@JsonProperty("nextCursor") String nextCursor) {
		this.prompts = prompts;
		this.nextCursor = nextCursor;
	}

	public List<Prompt> getPrompts() {
		return prompts;
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
		ListPromptsResult that = (ListPromptsResult) o;
		return Objects.equals(prompts, that.prompts) && Objects.equals(nextCursor, that.nextCursor);
	}

	@Override
	public int hashCode() {
		return Objects.hash(prompts, nextCursor);
	}

	@Override
	public String toString() {
		return "ListPromptsResult{" + "prompts=" + prompts + ", nextCursor='" + nextCursor + '\'' + '}';
	}

}
