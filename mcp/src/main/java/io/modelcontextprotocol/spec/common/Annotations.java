package io.modelcontextprotocol.spec.common;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Objects;

/**
 * 可选注解。客户端可用注解指导对象的使用或展示。
 *
 * @param audience 目标受众
 * @param priority 优先级，0~1
 */
@JsonInclude(JsonInclude.Include.NON_ABSENT)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Annotations {

	@JsonProperty("audience")
	private final List<Role> audience;

	@JsonProperty("priority")
	private final Double priority;

	public Annotations(@JsonProperty("audience") List<Role> audience, @JsonProperty("priority") Double priority) {
		this.audience = audience;
		this.priority = priority;
	}

	public List<Role> getAudience() {
		return audience;
	}

	public Double getPriority() {
		return priority;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		Annotations that = (Annotations) o;
		return Objects.equals(audience, that.audience) && Objects.equals(priority, that.priority);
	}

	@Override
	public int hashCode() {
		return Objects.hash(audience, priority);
	}

	@Override
	public String toString() {
		return "Annotations{" + "audience=" + audience + ", priority=" + priority + '}';
	}

}
