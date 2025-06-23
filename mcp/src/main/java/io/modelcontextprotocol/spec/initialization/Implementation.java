package io.modelcontextprotocol.spec.initialization;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;

/**
 * 实现信息。
 */
@JsonInclude(JsonInclude.Include.NON_ABSENT)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Implementation {

	@JsonProperty("name")
	private final String name;

	@JsonProperty("version")
	private final String version;

	public Implementation(@JsonProperty("name") String name, @JsonProperty("version") String version) {
		this.name = name;
		this.version = version;
	}

	public String getName() {
		return name;
	}

	public String getVersion() {
		return version;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		Implementation that = (Implementation) o;
		return Objects.equals(name, that.name) && Objects.equals(version, that.version);
	}

	@Override
	public int hashCode() {
		return Objects.hash(name, version);
	}

	@Override
	public String toString() {
		return "Implementation{" + "name='" + name + '\'' + ", version='" + version + '\'' + '}';
	}

}
