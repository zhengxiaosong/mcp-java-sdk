package io.modelcontextprotocol.spec.resource;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;

/**
 * 资源订阅请求。
 *
 * @param uri 资源URI
 */
@JsonInclude(JsonInclude.Include.NON_ABSENT)
@JsonIgnoreProperties(ignoreUnknown = true)
public class SubscribeRequest {

	@JsonProperty("uri")
	private final String uri;

	public SubscribeRequest(@JsonProperty("uri") String uri) {
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
		SubscribeRequest that = (SubscribeRequest) o;
		return Objects.equals(uri, that.uri);
	}

	@Override
	public int hashCode() {
		return Objects.hash(uri);
	}

	@Override
	public String toString() {
		return "SubscribeRequest{" + "uri='" + uri + '\'' + '}';
	}

}
