package io.modelcontextprotocol.spec.content;

import java.util.List;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.modelcontextprotocol.spec.common.Role;
import io.modelcontextprotocol.spec.resource.ResourceContents;

/**
 * 内嵌资源内容。
 *
 * @param audience 目标受众
 * @param priority 优先级
 * @param resource 资源内容
 */
public final class EmbeddedResource implements Content {

	private final List<Role> audience;

	private final Double priority;

	private final ResourceContents resource;

	@JsonCreator
	public EmbeddedResource(@JsonProperty("audience") List<Role> audience, @JsonProperty("priority") Double priority,
			@JsonProperty("resource") ResourceContents resource) {
		this.audience = audience;
		this.priority = priority;
		this.resource = resource;
	}

	@Override
	public String getType() {
		return "resource";
	}

	public List<Role> getAudience() {
		return this.audience;
	}

	public Double getPriority() {
		return this.priority;
	}

	public ResourceContents getResource() {
		return this.resource;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null || getClass() != obj.getClass()) {
			return false;
		}
		EmbeddedResource that = (EmbeddedResource) obj;
		return Objects.equals(this.audience, that.audience) && Objects.equals(this.priority, that.priority)
				&& Objects.equals(this.resource, that.resource);
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.audience, this.priority, this.resource);
	}

	@Override
	public String toString() {
		return "EmbeddedResource[" + "audience=" + this.audience + ", " + "priority=" + this.priority + ", "
				+ "resource=" + this.resource + ']';
	}

}
