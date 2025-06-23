package io.modelcontextprotocol.spec.content;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * 内容类型接口。
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({ @JsonSubTypes.Type(value = TextContent.class, name = "text"),
		@JsonSubTypes.Type(value = ImageContent.class, name = "image"),
		@JsonSubTypes.Type(value = EmbeddedResource.class, name = "resource") })
public interface Content {

	default String getType() {
		if (this instanceof TextContent) {
			return "text";
		}
		else if (this instanceof ImageContent) {
			return "image";
		}
		else if (this instanceof EmbeddedResource) {
			return "resource";
		}
		throw new IllegalArgumentException("Unknown content type: " + this);
	}

}
