package io.modelcontextprotocol.spec.sampling;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * LLM采样参数。
 *
 * @param hints 提示
 * @param costPriority 成本优先级
 * @param speedPriority 速度优先级
 * @param intelligencePriority 智能优先级
 */
@JsonInclude(JsonInclude.Include.NON_ABSENT)
@JsonIgnoreProperties(ignoreUnknown = true)
public final class ModelPreferences {

	@JsonProperty("hints")
	private final List<ModelHint> hints;

	@JsonProperty("costPriority")
	private final Double costPriority;

	@JsonProperty("speedPriority")
	private final Double speedPriority;

	@JsonProperty("intelligencePriority")
	private final Double intelligencePriority;

	public ModelPreferences(List<ModelHint> hints, Double costPriority, Double speedPriority,
			Double intelligencePriority) {
		this.hints = hints;
		this.costPriority = costPriority;
		this.speedPriority = speedPriority;
		this.intelligencePriority = intelligencePriority;
	}

	public List<ModelHint> getHints() {
		return this.hints;
	}

	public Double getCostPriority() {
		return this.costPriority;
	}

	public Double getSpeedPriority() {
		return this.speedPriority;
	}

	public Double getIntelligencePriority() {
		return this.intelligencePriority;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this)
			return true;
		if (obj == null || obj.getClass() != this.getClass())
			return false;
		var that = (ModelPreferences) obj;
		return Objects.equals(this.hints, that.hints) && Objects.equals(this.costPriority, that.costPriority)
				&& Objects.equals(this.speedPriority, that.speedPriority)
				&& Objects.equals(this.intelligencePriority, that.intelligencePriority);
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.hints, this.costPriority, this.speedPriority, this.intelligencePriority);
	}

	@Override
	public String toString() {
		return "ModelPreferences[" + "hints=" + this.hints + ", " + "costPriority=" + this.costPriority + ", "
				+ "speedPriority=" + this.speedPriority + ", " + "intelligencePriority=" + this.intelligencePriority
				+ ']';
	}

	public static Builder builder() {
		return new Builder();
	}

	public static class Builder {

		private List<ModelHint> hints;

		private Double costPriority;

		private Double speedPriority;

		private Double intelligencePriority;

		public Builder hints(List<ModelHint> hints) {
			this.hints = hints;
			return this;
		}

		public Builder addHint(String name) {
			if (this.hints == null) {
				this.hints = new ArrayList<>();
			}
			this.hints.add(new ModelHint(name));
			return this;
		}

		public Builder costPriority(Double costPriority) {
			this.costPriority = costPriority;
			return this;
		}

		public Builder speedPriority(Double speedPriority) {
			this.speedPriority = speedPriority;
			return this;
		}

		public Builder intelligencePriority(Double intelligencePriority) {
			this.intelligencePriority = intelligencePriority;
			return this;
		}

		public ModelPreferences build() {
			return new ModelPreferences(this.hints, this.costPriority, this.speedPriority, this.intelligencePriority);
		}

	}

}
