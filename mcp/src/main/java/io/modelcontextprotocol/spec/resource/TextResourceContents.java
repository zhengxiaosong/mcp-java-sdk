package io.modelcontextprotocol.spec.resource;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 资源的文本内容。
 *
 * @param uri 资源URI
 * @param mimeType MIME类型
 * @param text 文本内容
 */
public final class TextResourceContents implements ResourceContents {

	private final String uri;

	private final String mimeType;

	private final String text;

	@JsonCreator
	public TextResourceContents(@JsonProperty("uri") String uri, @JsonProperty("mimeType") String mimeType,
			@JsonProperty("text") String text) {
		this.uri = uri;
		this.mimeType = mimeType;
		this.text = text;
	}

	@Override
	public String getUri() {
		return this.uri;
	}

	@Override
	public String getMimeType() {
		return this.mimeType;
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
		TextResourceContents that = (TextResourceContents) obj;
		return Objects.equals(this.uri, that.uri) && Objects.equals(this.mimeType, that.mimeType)
				&& Objects.equals(this.text, that.text);
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.uri, this.mimeType, this.text);
	}

	@Override
	public String toString() {
		return "TextResourceContents[" + "uri=" + this.uri + ", " + "mimeType=" + this.mimeType + ", " + "text="
				+ this.text + ']';
	}

}
