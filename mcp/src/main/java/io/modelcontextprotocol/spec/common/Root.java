package io.modelcontextprotocol.spec.common;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;

/**
 * 代表服务器可操作的根目录或文件。
 *
 * @param uri 根的URI，当前必须以file://开头
 * @param name 根的可选名称
 */
@JsonInclude(JsonInclude.Include.NON_ABSENT)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Root {

	@JsonProperty("uri")
	private final String uri;

	@JsonProperty("name")
	private final String name;

	public Root(@JsonProperty("uri") String uri, @JsonProperty("name") String name) {
		this.uri = uri;
		this.name = name;
	}

	public String getUri() {
		return uri;
	}

	public String getName() {
		return name;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		Root root = (Root) o;
		return Objects.equals(uri, root.uri) && Objects.equals(name, root.name);
	}

	@Override
	public int hashCode() {
		return Objects.hash(uri, name);
	}

	@Override
	public String toString() {
		return "Root{" + "uri='" + uri + '\'' + ", name='" + name + '\'' + '}';
	}

}
