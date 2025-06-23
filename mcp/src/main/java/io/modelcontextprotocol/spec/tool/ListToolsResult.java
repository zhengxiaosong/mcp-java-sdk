package io.modelcontextprotocol.spec.tool;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Objects;

/**
 * 工具列表结果。
 *
 * @param tools 工具列表
 */
@JsonInclude(JsonInclude.Include.NON_ABSENT)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ListToolsResult {

	@JsonProperty("tools")
	private final List<Tool> tools;

	@JsonProperty("nextCursor")
	private final String nextCursor;

	public ListToolsResult(@JsonProperty("tools") List<Tool> tools, @JsonProperty("nextCursor") String nextCursor) {
		this.tools = tools;
		this.nextCursor = nextCursor;
	}

	public List<Tool> getTools() {
		return tools;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		ListToolsResult that = (ListToolsResult) o;
		return Objects.equals(tools, that.tools);
	}

	@Override
	public int hashCode() {
		return Objects.hash(tools);
	}

	@Override
	public String toString() {
		return "ListToolsResult{" + "tools=" + tools + '}';
	}

}
