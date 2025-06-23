package io.modelcontextprotocol.spec.resource;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Objects;

/**
 * 资源模板列表响应。
 *
 * @param resourceTemplates 资源模板列表
 * @param nextCursor 分页游标
 */
@JsonInclude(JsonInclude.Include.NON_ABSENT)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ListResourceTemplatesResult {

	@JsonProperty("resourceTemplates")
	private final List<ResourceTemplate> resourceTemplates;

	@JsonProperty("nextCursor")
	private final String nextCursor;

	public ListResourceTemplatesResult(@JsonProperty("resourceTemplates") List<ResourceTemplate> resourceTemplates,
			@JsonProperty("nextCursor") String nextCursor) {
		this.resourceTemplates = resourceTemplates;
		this.nextCursor = nextCursor;
	}

	public List<ResourceTemplate> getResourceTemplates() {
		return resourceTemplates;
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
		ListResourceTemplatesResult that = (ListResourceTemplatesResult) o;
		return Objects.equals(resourceTemplates, that.resourceTemplates) && Objects.equals(nextCursor, that.nextCursor);
	}

	@Override
	public int hashCode() {
		return Objects.hash(resourceTemplates, nextCursor);
	}

	@Override
	public String toString() {
		return "ListResourceTemplatesResult{" + "resourceTemplates=" + resourceTemplates + ", nextCursor='" + nextCursor
				+ '\'' + '}';
	}

}
