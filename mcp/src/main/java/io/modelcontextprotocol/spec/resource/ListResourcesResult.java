package io.modelcontextprotocol.spec.resource;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Objects;

/**
 * 资源列表响应。
 *
 * @param resources 资源列表
 * @param nextCursor 分页游标
 */
@JsonInclude(JsonInclude.Include.NON_ABSENT)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ListResourcesResult {

	@JsonProperty("resources")
	private final List<Resource> resources;

	@JsonProperty("nextCursor")
	private final String nextCursor;

	public ListResourcesResult(@JsonProperty("resources") List<Resource> resources,
			@JsonProperty("nextCursor") String nextCursor) {
		this.resources = resources;
		this.nextCursor = nextCursor;
	}

	public List<Resource> getResources() {
		return resources;
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
		ListResourcesResult that = (ListResourcesResult) o;
		return Objects.equals(resources, that.resources) && Objects.equals(nextCursor, that.nextCursor);
	}

	@Override
	public int hashCode() {
		return Objects.hash(resources, nextCursor);
	}

	@Override
	public String toString() {
		return "ListResourcesResult{" + "resources=" + resources + ", nextCursor='" + nextCursor + '\'' + '}';
	}

}
