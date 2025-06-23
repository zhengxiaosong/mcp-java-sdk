package io.modelcontextprotocol.spec.resource;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.modelcontextprotocol.spec.common.Annotated;
import io.modelcontextprotocol.spec.common.Annotations;
import java.util.Objects;

/**
 * 资源模板，支持参数化URI。
 *
 * @param uriTemplate URI模板
 * @param name 名称
 * @param description 描述
 * @param mimeType MIME类型
 * @param annotations 可选注解
 */
@JsonInclude(JsonInclude.Include.NON_ABSENT)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ResourceTemplate implements Annotated {

	@JsonProperty("uriTemplate")
	private final String uriTemplate;

	@JsonProperty("name")
	private final String name;

	@JsonProperty("description")
	private final String description;

	@JsonProperty("mimeType")
	private final String mimeType;

	@JsonProperty("annotations")
	private final Annotations annotations;

	public ResourceTemplate(@JsonProperty("uriTemplate") String uriTemplate, @JsonProperty("name") String name,
			@JsonProperty("description") String description, @JsonProperty("mimeType") String mimeType,
			@JsonProperty("annotations") Annotations annotations) {
		this.uriTemplate = uriTemplate;
		this.name = name;
		this.description = description;
		this.mimeType = mimeType;
		this.annotations = annotations;
	}

	public String getUriTemplate() {
		return uriTemplate;
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
		ResourceTemplate that = (ResourceTemplate) o;
		return Objects.equals(uriTemplate, that.uriTemplate) && Objects.equals(name, that.name)
				&& Objects.equals(description, that.description) && Objects.equals(mimeType, that.mimeType)
				&& Objects.equals(annotations, that.annotations);
	}

	@Override
	public int hashCode() {
		return Objects.hash(uriTemplate, name, description, mimeType, annotations);
	}

	@Override
	public String toString() {
		return "ResourceTemplate{" + "uriTemplate='" + uriTemplate + '\'' + ", name='" + name + '\'' + ", description='"
				+ description + '\'' + ", mimeType='" + mimeType + '\'' + ", annotations=" + annotations + '}';
	}

}
