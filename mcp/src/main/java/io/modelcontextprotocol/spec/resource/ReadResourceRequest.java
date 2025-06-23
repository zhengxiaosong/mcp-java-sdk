package io.modelcontextprotocol.spec.resource;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;

/**
 * 资源读取请求。
 *
 * @param uri 资源URI
 */
@JsonInclude(JsonInclude.Include.NON_ABSENT)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ReadResourceRequest {

	@JsonProperty("uri")
	private final String uri;

	public ReadResourceRequest(@JsonProperty("uri") String uri) {
		this.uri = uri;
	}

	public String getUri() {
		return uri;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		ReadResourceRequest that = (ReadResourceRequest) o;
		return Objects.equals(uri, that.uri);
	}

	@Override
	public int hashCode() {
		return Objects.hash(uri);
	}

	@Override
	public String toString() {
		return "ReadResourceRequest{" + "uri='" + uri + '\'' + '}';
	}

}
