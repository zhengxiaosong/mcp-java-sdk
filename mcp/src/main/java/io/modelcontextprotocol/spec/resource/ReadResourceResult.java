package io.modelcontextprotocol.spec.resource;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Objects;

/**
 * 资源读取响应。
 *
 * @param contents 资源内容列表
 */
@JsonInclude(JsonInclude.Include.NON_ABSENT)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ReadResourceResult {

	@JsonProperty("contents")
	private final List<ResourceContents> contents;

	public ReadResourceResult(@JsonProperty("contents") List<ResourceContents> contents) {
		this.contents = contents;
	}

	public List<ResourceContents> getContents() {
		return contents;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		ReadResourceResult that = (ReadResourceResult) o;
		return Objects.equals(contents, that.contents);
	}

	@Override
	public int hashCode() {
		return Objects.hash(contents);
	}

	@Override
	public String toString() {
		return "ReadResourceResult{" + "contents=" + contents + '}';
	}

}
