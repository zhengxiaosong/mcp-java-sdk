package io.modelcontextprotocol.spec.sampling;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;

/**
 * LLM模型提示。
 *
 * @param name 模型名称
 */
@JsonInclude(JsonInclude.Include.NON_ABSENT)
@JsonIgnoreProperties(ignoreUnknown = true)
public final class ModelHint {

	@JsonProperty("name")
	private final String name;

	public ModelHint(String name) {
		this.name = name;
	}

	public static ModelHint of(String name) {
		return new ModelHint(name);
	}

	public String name() {
		return this.name;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this)
			return true;
		if (obj == null || obj.getClass() != this.getClass())
			return false;
		var that = (ModelHint) obj;
		return Objects.equals(this.name, that.name);
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.name);
	}

	@Override
	public String toString() {
		return "ModelHint[" + "name=" + this.name + ']';
	}

}
