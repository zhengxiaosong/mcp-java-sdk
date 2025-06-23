package io.modelcontextprotocol.spec.resource;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;

/**
 * 资源内容。
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.DEDUCTION, include = As.PROPERTY)
@JsonSubTypes({ @JsonSubTypes.Type(value = TextResourceContents.class),
		@JsonSubTypes.Type(value = BlobResourceContents.class) })
public interface ResourceContents {

	/**
	 * The URI of this resource.
	 * @return the URI of this resource.
	 */
	String getUri();

	/**
	 * The MIME type of this resource.
	 * @return the MIME type of this resource.
	 */
	String getMimeType();

}
