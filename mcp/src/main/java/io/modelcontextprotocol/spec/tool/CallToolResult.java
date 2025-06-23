package io.modelcontextprotocol.spec.tool;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.modelcontextprotocol.spec.content.Content;
import io.modelcontextprotocol.spec.content.TextContent;
import io.modelcontextprotocol.util.Assert;

/**
 * The server's response to a tools/call request from the client.
 *
 * @param content A list of content items representing the tool's output. Each item can be
 * text, an image, or an embedded resource.
 * @param isError If true, indicates that the tool execution failed and the content
 * contains error information. If false or absent, indicates successful execution.
 */
@JsonInclude(JsonInclude.Include.NON_ABSENT)
@JsonIgnoreProperties(ignoreUnknown = true)
public class CallToolResult {

	@JsonProperty("content")
	private final List<Content> content;

	@JsonProperty("isError")
	private final Boolean isError;

	public CallToolResult(@JsonProperty("content") List<Content> content, @JsonProperty("isError") Boolean isError) {
		this.content = content;
		this.isError = isError;
	}

	/**
	 * Creates a new instance of {@link CallToolResult} with a string containing the tool
	 * result.
	 * @param content The content of the tool result. This will be mapped to a one-sized
	 * list with a {@link TextContent} element.
	 * @param isError If true, indicates that the tool execution failed and the content
	 * contains error information. If false or absent, indicates successful execution.
	 */
	public CallToolResult(String content, Boolean isError) {
		this(List.of(new TextContent(content)), isError);
	}

	public List<Content> getContent() {
		return this.content;
	}

	public Boolean isError() {
		return this.isError;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		CallToolResult that = (CallToolResult) o;
		return Objects.equals(this.content, that.content) && Objects.equals(this.isError, that.isError);
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.content, this.isError);
	}

	@Override
	public String toString() {
		return "CallToolResult{" + "content=" + this.content + ", isError=" + this.isError + '}';
	}

	/**
	 * Creates a builder for {@link CallToolResult}.
	 * @return a new builder instance
	 */
	public static Builder builder() {
		return new Builder();
	}

	/**
	 * Builder for {@link CallToolResult}.
	 */
	public static class Builder {

		private List<Content> content = new ArrayList<>();

		private Boolean isError;

		/**
		 * Sets the content list for the tool result.
		 * @param content the content list
		 * @return this builder
		 */
		public Builder content(List<Content> content) {
			Assert.notNull(content, "content must not be null");
			this.content = content;
			return this;
		}

		/**
		 * Sets the text content for the tool result.
		 * @param textContent the text content
		 * @return this builder
		 */
		public Builder textContent(List<String> textContent) {
			Assert.notNull(textContent, "textContent must not be null");
			textContent.stream().map(TextContent::new).forEach(this.content::add);
			return this;
		}

		/**
		 * Adds a content item to the tool result.
		 * @param contentItem the content item to add
		 * @return this builder
		 */
		public Builder addContent(Content contentItem) {
			Assert.notNull(contentItem, "contentItem must not be null");
			if (this.content == null) {
				this.content = new ArrayList<>();
			}
			this.content.add(contentItem);
			return this;
		}

		/**
		 * Adds a text content item to the tool result.
		 * @param text the text content
		 * @return this builder
		 */
		public Builder addTextContent(String text) {
			Assert.notNull(text, "text must not be null");
			return addContent(new TextContent(text));
		}

		/**
		 * Sets whether the tool execution resulted in an error.
		 * @param isError true if the tool execution failed, false otherwise
		 * @return this builder
		 */
		public Builder isError(Boolean isError) {
			Assert.notNull(isError, "isError must not be null");
			this.isError = isError;
			return this;
		}

		/**
		 * Builds a new {@link CallToolResult} instance.
		 * @return a new CallToolResult instance
		 */
		public CallToolResult build() {
			return new CallToolResult(this.content, this.isError);
		}

	}

}
