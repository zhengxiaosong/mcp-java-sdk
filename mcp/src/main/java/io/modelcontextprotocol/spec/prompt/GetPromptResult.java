package io.modelcontextprotocol.spec.prompt;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Objects;

/**
 * 获取提示响应。
 *
 * @param description 描述
 * @param messages 消息列表
 */
@JsonInclude(JsonInclude.Include.NON_ABSENT)
@JsonIgnoreProperties(ignoreUnknown = true)
public class GetPromptResult {

	@JsonProperty("description")
	private final String description;

	@JsonProperty("messages")
	private final List<PromptMessage> messages;

	public GetPromptResult(@JsonProperty("description") String description,
			@JsonProperty("messages") List<PromptMessage> messages) {
		this.description = description;
		this.messages = messages;
	}

	public String getDescription() {
		return description;
	}

	public List<PromptMessage> getMessages() {
		return messages;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		GetPromptResult that = (GetPromptResult) o;
		return Objects.equals(description, that.description) && Objects.equals(messages, that.messages);
	}

	@Override
	public int hashCode() {
		return Objects.hash(description, messages);
	}

	@Override
	public String toString() {
		return "GetPromptResult{" + "description='" + description + '\'' + ", messages=" + messages + '}';
	}

}
