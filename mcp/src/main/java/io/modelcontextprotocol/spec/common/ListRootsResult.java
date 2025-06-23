package io.modelcontextprotocol.spec.common;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Objects;

/**
 * roots/list请求的响应，包含所有可操作的根。
 *
 * @param roots 根对象列表
 */
@JsonInclude(JsonInclude.Include.NON_ABSENT)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ListRootsResult {

	@JsonProperty("roots")
	private final List<Root> roots;

	public ListRootsResult(@JsonProperty("roots") List<Root> roots) {
		this.roots = roots;
	}

	public List<Root> getRoots() {
		return roots;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		ListRootsResult that = (ListRootsResult) o;
		return Objects.equals(roots, that.roots);
	}

	@Override
	public int hashCode() {
		return Objects.hash(roots);
	}

	@Override
	public String toString() {
		return "ListRootsResult{" + "roots=" + roots + '}';
	}

}
