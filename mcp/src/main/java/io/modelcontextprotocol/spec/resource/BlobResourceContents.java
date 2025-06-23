package io.modelcontextprotocol.spec.resource;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 资源的二进制内容。
 *
 * @param uri 资源URI
 * @param mimeType MIME类型
 * @param blob base64编码的二进制内容
 */
public final class BlobResourceContents implements ResourceContents {

	private final String uri;

	private final String mimeType;

	private final String blob;

	@JsonCreator
	public BlobResourceContents(@JsonProperty("uri") String uri, @JsonProperty("mimeType") String mimeType,
			@JsonProperty("blob") String blob) {
		this.uri = uri;
		this.mimeType = mimeType;
		this.blob = blob;
	}

	@Override
	public String getUri() {
		return this.uri;
	}

	@Override
	public String getMimeType() {
		return this.mimeType;
	}

	public String getBlob() {
		return this.blob;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null || getClass() != obj.getClass()) {
			return false;
		}
		BlobResourceContents that = (BlobResourceContents) obj;
		return Objects.equals(this.uri, that.uri) && Objects.equals(this.mimeType, that.mimeType)
				&& Objects.equals(this.blob, that.blob);
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.uri, this.mimeType, this.blob);
	}

	@Override
	public String toString() {
		return "BlobResourceContents[" + "uri=" + this.uri + ", " + "mimeType=" + this.mimeType + ", " + "blob="
				+ this.blob + ']';
	}

}
