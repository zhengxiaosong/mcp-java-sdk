package io.modelcontextprotocol.spec.resource;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.modelcontextprotocol.spec.common.Annotated;
import io.modelcontextprotocol.spec.common.Annotations;
import java.util.Objects;

/**
 * 服务器可读取的已知资源。
 *
 * @param uri 资源URI
 * @param name 资源名称
 * @param description 资源描述
 * @param mimeType 资源MIME类型
 * @param annotations 可选注解
 */
@JsonInclude(JsonInclude.Include.NON_ABSENT)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Resource implements Annotated {

	@JsonProperty("uri")
	private final String uri;

	@JsonProperty("name")
	private final String name;

	@JsonProperty("description")
	private final String description;

	@JsonProperty("mimeType")
	private final String mimeType;

	@JsonProperty("annotations")
	private final Annotations annotations;

	public Resource(@JsonProperty("uri") String uri, @JsonProperty("name") String name,
			@JsonProperty("description") String description, @JsonProperty("mimeType") String mimeType,
			@JsonProperty("annotations") Annotations annotations) {
		this.uri = uri;
		this.name = name;
		this.description = description;
		this.mimeType = mimeType;
		this.annotations = annotations;
	}

	public String getUri() {
		return uri;
	}

	public String getName() {
		return name;
	}

	public String getDescription() {
		return description;
	}

	public String getMimeType() {
		return mimeType;
	}

	public Annotations getAnnotations() {
		return annotations;
	}

	@Override
	public Annotations annotations() {
		return getAnnotations();
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		Resource resource = (Resource) o;
		return Objects.equals(uri, resource.uri) && Objects.equals(name, resource.name)
				&& Objects.equals(description, resource.description) && Objects.equals(mimeType, resource.mimeType)
				&& Objects.equals(annotations, resource.annotations);
	}

	@Override
	public int hashCode() {
		return Objects.hash(uri, name, description, mimeType, annotations);
	}

	@Override
	public String toString() {
		return "Resource{" + "uri='" + uri + '\'' + ", name='" + name + '\'' + ", description='" + description + '\''
				+ ", mimeType='" + mimeType + '\'' + ", annotations=" + annotations + '}';
	}

}
