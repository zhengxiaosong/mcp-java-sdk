package io.modelcontextprotocol.spec.content;

import java.util.List;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.modelcontextprotocol.spec.common.Role;

/**
 * 文本内容。
 *
 * @param text 文本内容
 */
public final class TextContent implements Content {

	private final List<Role> audience;

	private final Double priority;

	private final String text;

	public TextContent(String text) {
		this(null, null, text);
	}

	@JsonCreator
	public TextContent(@JsonProperty("audience") List<Role> audience, @JsonProperty("priority") Double priority,
			@JsonProperty("text") String text) {
		this.audience = audience;
		this.priority = priority;
		this.text = text;
	}

	@Override
	public String getType() {
		return "text";
	}

	public List<Role> getAudience() {
		return this.audience;
	}

	public Double getPriority() {
		return this.priority;
	}

	public String getText() {
		return this.text;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null || getClass() != obj.getClass()) {
			return false;
		}
		TextContent that = (TextContent) obj;
		return Objects.equals(this.audience, that.audience) && Objects.equals(this.priority, that.priority)
				&& Objects.equals(this.text, that.text);
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.audience, this.priority, this.text);
	}

	@Override
	public String toString() {
		return "TextContent[" + "audience=" + this.audience + ", " + "priority=" + this.priority + ", " + "text="
				+ this.text + ']';
	}

}
