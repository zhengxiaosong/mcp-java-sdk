package io.modelcontextprotocol.spec.sampling;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.modelcontextprotocol.spec.common.Request;

public final class CreateMessageRequest implements Request {

	private final List<SamplingMessage> messages;

	private final ModelPreferences modelPreferences;

	private final String systemPrompt;

	private final ContextInclusionStrategy includeContext;

	private final Double temperature;

	private final Integer maxTokens;

	private final List<String> stopSequences;

	private final Map<String, Object> metadata;

	public enum ContextInclusionStrategy {

		@JsonProperty("none")
		NONE, @JsonProperty("thisServer")
		THIS_SERVER, @JsonProperty("allServers")
		ALL_SERVERS

	}

	@JsonCreator
	public CreateMessageRequest(@JsonProperty("messages") List<SamplingMessage> messages,
			@JsonProperty("modelPreferences") ModelPreferences modelPreferences,
			@JsonProperty("systemPrompt") String systemPrompt,
			@JsonProperty("includeContext") ContextInclusionStrategy includeContext,
			@JsonProperty("temperature") Double temperature, @JsonProperty("maxTokens") Integer maxTokens,
			@JsonProperty("stopSequences") List<String> stopSequences,
			@JsonProperty("metadata") Map<String, Object> metadata) {
		this.messages = messages;
		this.modelPreferences = modelPreferences;
		this.systemPrompt = systemPrompt;
		this.includeContext = includeContext;
		this.temperature = temperature;
		this.maxTokens = maxTokens;
		this.stopSequences = stopSequences;
		this.metadata = metadata;
	}

	public static Builder builder() {
		return new Builder();
	}

	public List<SamplingMessage> getMessages() {
		return this.messages;
	}

	public ModelPreferences getModelPreferences() {
		return this.modelPreferences;
	}

	public String getSystemPrompt() {
		return this.systemPrompt;
	}

	public ContextInclusionStrategy getIncludeContext() {
		return this.includeContext;
	}

	public Double getTemperature() {
		return this.temperature;
	}

	public Integer getMaxTokens() {
		return this.maxTokens;
	}

	public List<String> getStopSequences() {
		return this.stopSequences;
	}

	public Map<String, Object> getMetadata() {
		return this.metadata;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null || getClass() != obj.getClass()) {
			return false;
		}
		CreateMessageRequest that = (CreateMessageRequest) obj;
		return Objects.equals(this.messages, that.messages)
				&& Objects.equals(this.modelPreferences, that.modelPreferences)
				&& Objects.equals(this.systemPrompt, that.systemPrompt) && this.includeContext == that.includeContext
				&& Objects.equals(this.temperature, that.temperature) && Objects.equals(this.maxTokens, that.maxTokens)
				&& Objects.equals(this.stopSequences, that.stopSequences)
				&& Objects.equals(this.metadata, that.metadata);
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.messages, this.modelPreferences, this.systemPrompt, this.includeContext,
				this.temperature, this.maxTokens, this.stopSequences, this.metadata);
	}

	@Override
	public String toString() {
		return "CreateMessageRequest[" + "messages=" + this.messages + ", " + "modelPreferences="
				+ this.modelPreferences + ", " + "systemPrompt=" + this.systemPrompt + ", " + "includeContext="
				+ this.includeContext + ", " + "temperature=" + this.temperature + ", " + "maxTokens=" + this.maxTokens
				+ ", " + "stopSequences=" + this.stopSequences + ", " + "metadata=" + this.metadata + ']';
	}

	public static class Builder {

		private List<SamplingMessage> messages;

		private ModelPreferences modelPreferences;

		private String systemPrompt;

		private ContextInclusionStrategy includeContext;

		private Double temperature;

		private Integer maxTokens;

		private List<String> stopSequences;

		private Map<String, Object> metadata;

		public Builder messages(List<SamplingMessage> messages) {
			this.messages = messages;
			return this;
		}

		public Builder modelPreferences(ModelPreferences modelPreferences) {
			this.modelPreferences = modelPreferences;
			return this;
		}

		public Builder systemPrompt(String systemPrompt) {
			this.systemPrompt = systemPrompt;
			return this;
		}

		public Builder includeContext(ContextInclusionStrategy includeContext) {
			this.includeContext = includeContext;
			return this;
		}

		public Builder temperature(Double temperature) {
			this.temperature = temperature;
			return this;
		}

		public Builder maxTokens(Integer maxTokens) {
			this.maxTokens = maxTokens;
			return this;
		}

		public Builder stopSequences(List<String> stopSequences) {
			this.stopSequences = stopSequences;
			return this;
		}

		public Builder metadata(Map<String, Object> metadata) {
			this.metadata = metadata;
			return this;
		}

		public CreateMessageRequest build() {
			return new CreateMessageRequest(this.messages, this.modelPreferences, this.systemPrompt,
					this.includeContext, this.temperature, this.maxTokens, this.stopSequences, this.metadata);
		}

	}

}
