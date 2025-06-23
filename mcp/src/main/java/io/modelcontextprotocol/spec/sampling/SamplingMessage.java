package io.modelcontextprotocol.spec.sampling;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.modelcontextprotocol.spec.common.Role;
import io.modelcontextprotocol.spec.content.Content;
import java.util.Objects;

/**
 * 采样消息。
 *
 * @param role 角色
 * @param content 内容
 */
@JsonInclude(JsonInclude.Include.NON_ABSENT)
@JsonIgnoreProperties(ignoreUnknown = true)
public final class SamplingMessage {

	private final Role role;

	private final Content content;

	public SamplingMessage(@JsonProperty("role") Role role, @JsonProperty("content") Content content) {
		this.role = role;
		this.content = content;
	}

	public Role getRole() {
		return this.role;
	}

	public Content getContent() {
		return this.content;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this)
			return true;
		if (obj == null || obj.getClass() != this.getClass())
			return false;
		var that = (SamplingMessage) obj;
		return Objects.equals(this.role, that.role) && Objects.equals(this.content, that.content);
	}

	@Override
	public int hashCode() {
		return Objects.hash(role, content);
	}

	@Override
	public String toString() {
		return "SamplingMessage[" + "role=" + role + ", " + "content=" + content + ']';
	}

}
