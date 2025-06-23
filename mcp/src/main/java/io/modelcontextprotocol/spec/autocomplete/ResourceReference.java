package io.modelcontextprotocol.spec.autocomplete;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;

/**
 * 资源引用。
 *
 * @param uri 资源URI
 */
@JsonInclude(JsonInclude.Include.NON_ABSENT)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ResourceReference implements CompleteReference {

	@JsonProperty("type")
	private final String type = "ref/resource";

	@JsonProperty("uri")
	private final String uri;

	public ResourceReference(@JsonProperty("uri") String uri) {
		this.uri = uri;
	}

	public String getUri() {
		return uri;
	}

	@Override
	public String getType() {
		return type;
	}

	@Override
	public String getIdentifier() {
		return uri;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		ResourceReference that = (ResourceReference) o;
		return Objects.equals(type, that.type) && Objects.equals(uri, that.uri);
	}

	@Override
	public int hashCode() {
		return Objects.hash(type, uri);
	}

	@Override
	public String toString() {
		return "ResourceReference{" + "type='" + type + '\'' + ", uri='" + uri + '\'' + '}';
	}

}
