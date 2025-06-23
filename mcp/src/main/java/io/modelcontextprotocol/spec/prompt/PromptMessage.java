package io.modelcontextprotocol.spec.prompt;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.modelcontextprotocol.spec.common.Role;
import io.modelcontextprotocol.spec.content.Content;
import java.util.Objects;

/**
 * 提示消息。
 *
 * @param role 角色
 * @param content 消息内容
 */
@JsonInclude(JsonInclude.Include.NON_ABSENT)
@JsonIgnoreProperties(ignoreUnknown = true)
public class PromptMessage {

	@JsonProperty("role")
	private final Role role;

	@JsonProperty("content")
	private final Content content;

	public PromptMessage(@JsonProperty("role") Role role, @JsonProperty("content") Content content) {
		this.role = role;
		this.content = content;
	}

	public Role getRole() {
		return role;
	}

	public Content getContent() {
		return content;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		PromptMessage that = (PromptMessage) o;
		return Objects.equals(role, that.role) && Objects.equals(content, that.content);
	}

	@Override
	public int hashCode() {
		return Objects.hash(role, content);
	}

	@Override
	public String toString() {
		return "PromptMessage{" + "role=" + role + ", content=" + content + '}';
	}

}
