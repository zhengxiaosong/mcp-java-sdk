package io.modelcontextprotocol.spec.sampling;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.modelcontextprotocol.spec.common.Annotations;
import io.modelcontextprotocol.spec.common.Role;
import io.modelcontextprotocol.spec.content.Content;
import io.modelcontextprotocol.spec.content.TextContent;

/**
 * 创建消息结果。
 *
 * @param role 角色
 * @param content 内容
 * @param model 模型
 * @param stopReason 停止原因
 * @param stopSequence 停止序列
 * @param annotations 注解
 */
@JsonInclude(JsonInclude.Include.NON_ABSENT)
@JsonIgnoreProperties(ignoreUnknown = true)
public class CreateMessageResult {

	public enum StopReason {

		/**
		 * AI a natural turn conclusion.
		 */
		@JsonProperty("end_turn")
		END_TURN,
		/**
		 * The model generated the maximum number of tokens permitted by the server.
		 */
		@JsonProperty("max_tokens")
		MAX_TOKENS,
		/**
		 * The model generated a text sequence that matches one of the provided
		 * {@link ModelPreferences#stopSequences stop sequences}.
		 */
		@JsonProperty("stop_sequence")
		STOP_SEQUENCE

	}

	@JsonProperty("role")
	private final Role role;

	@JsonProperty("content")
	private final Content content;

	@JsonProperty("model")
	private final String model;

	@JsonProperty("stopReason")
	private final StopReason stopReason;

	@JsonProperty("stopSequence")
	private final String stopSequence;

	@JsonProperty(value = "annotations")
	@JsonInclude(JsonInclude.Include.ALWAYS)
	private final Annotations annotations;

	public CreateMessageResult(@JsonProperty("role") Role role, @JsonProperty("content") Content content,
			@JsonProperty("model") String model, @JsonProperty("stopReason") StopReason stopReason) {
		this(role, content, model, stopReason, null, null);
	}

	public CreateMessageResult(@JsonProperty("role") Role role, @JsonProperty("content") Content content,
			@JsonProperty("model") String model, @JsonProperty("stopReason") StopReason stopReason,
			@JsonProperty("stopSequence") String stopSequence,
			@JsonProperty("annotations") @JsonInclude(JsonInclude.Include.ALWAYS) Annotations annotations) {
		this.role = role;
		this.content = content;
		this.model = model;
		this.stopReason = stopReason;
		this.stopSequence = stopSequence;
		this.annotations = annotations;
	}

	public Role getRole() {
		return this.role;
	}

	public Content getContent() {
		return this.content;
	}

	public String getModel() {
		return this.model;
	}

	public StopReason getStopReason() {
		return this.stopReason;
	}

	public String getStopSequence() {
		return this.stopSequence;
	}

	public Annotations getAnnotations() {
		return this.annotations;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		CreateMessageResult that = (CreateMessageResult) o;
		return this.role == that.role && Objects.equals(this.content, that.content)
				&& Objects.equals(this.model, that.model) && this.stopReason == that.stopReason
				&& Objects.equals(this.stopSequence, that.stopSequence)
				&& Objects.equals(this.annotations, that.annotations);
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.role, this.content, this.model, this.stopReason, this.stopSequence, this.annotations);
	}

	@Override
	public String toString() {
		return "CreateMessageResult{" + "role=" + this.role + ", content=" + this.content + ", model='" + this.model
				+ '\'' + ", stopReason=" + this.stopReason + ", stopSequence='" + this.stopSequence + '\''
				+ ", annotations=" + this.annotations + '}';
	}

	public static Builder builder() {
		return new Builder();
	}

	public static class Builder {

		private Role role = Role.ASSISTANT;

		private Content content;

		private String model;

		private StopReason stopReason = StopReason.END_TURN;

		public Builder role(Role role) {
			this.role = role;
			return this;
		}

		public Builder content(Content content) {
			this.content = content;
			return this;
		}

		public Builder model(String model) {
			this.model = model;
			return this;
		}

		public Builder stopReason(StopReason stopReason) {
			this.stopReason = stopReason;
			return this;
		}

		public Builder message(String message) {
			this.content = new TextContent(message);
			return this;
		}

		public CreateMessageResult build() {
			return new CreateMessageResult(this.role, this.content, this.model, this.stopReason);
		}

	}

}
