package io.modelcontextprotocol.spec.initialization;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;
import java.util.Objects;

/**
 * 服务器能力声明。
 */
@JsonInclude(JsonInclude.Include.NON_ABSENT)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ServerCapabilities {

	@JsonProperty("completions")
	private final CompletionCapabilities completions;

	@JsonProperty("experimental")
	private final Map<String, Object> experimental;

	@JsonProperty("logging")
	private final LoggingCapabilities logging;

	@JsonProperty("prompts")
	private final PromptCapabilities prompts;

	@JsonProperty("resources")
	private final ResourceCapabilities resources;

	@JsonProperty("tools")
	private final ToolCapabilities tools;

	public ServerCapabilities(@JsonProperty("completions") CompletionCapabilities completions,
			@JsonProperty("experimental") Map<String, Object> experimental,
			@JsonProperty("logging") LoggingCapabilities logging, @JsonProperty("prompts") PromptCapabilities prompts,
			@JsonProperty("resources") ResourceCapabilities resources, @JsonProperty("tools") ToolCapabilities tools) {
		this.completions = completions;
		this.experimental = experimental;
		this.logging = logging;
		this.prompts = prompts;
		this.resources = resources;
		this.tools = tools;
	}

	public CompletionCapabilities getCompletions() {
		return completions;
	}

	public Map<String, Object> getExperimental() {
		return experimental;
	}

	public LoggingCapabilities getLogging() {
		return logging;
	}

	public PromptCapabilities getPrompts() {
		return prompts;
	}

	public ResourceCapabilities getResources() {
		return resources;
	}

	public ToolCapabilities getTools() {
		return tools;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		ServerCapabilities that = (ServerCapabilities) o;
		return Objects.equals(completions, that.completions) && Objects.equals(experimental, that.experimental)
				&& Objects.equals(logging, that.logging) && Objects.equals(prompts, that.prompts)
				&& Objects.equals(resources, that.resources) && Objects.equals(tools, that.tools);
	}

	@Override
	public int hashCode() {
		return Objects.hash(completions, experimental, logging, prompts, resources, tools);
	}

	@Override
	public String toString() {
		return "ServerCapabilities{" + "completions=" + completions + ", experimental=" + experimental + ", logging="
				+ logging + ", prompts=" + prompts + ", resources=" + resources + ", tools=" + tools + '}';
	}

	@JsonInclude(JsonInclude.Include.NON_ABSENT)
	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class CompletionCapabilities {

		public CompletionCapabilities() {
		}

		@Override
		public boolean equals(Object o) {
			return o instanceof CompletionCapabilities;
		}

		@Override
		public int hashCode() {
			return 0;
		}

		@Override
		public String toString() {
			return "CompletionCapabilities{}";
		}

	}

	@JsonInclude(JsonInclude.Include.NON_ABSENT)
	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class LoggingCapabilities {

		public LoggingCapabilities() {
		}

		@Override
		public boolean equals(Object o) {
			return o instanceof LoggingCapabilities;
		}

		@Override
		public int hashCode() {
			return 0;
		}

		@Override
		public String toString() {
			return "LoggingCapabilities{}";
		}

	}

	@JsonInclude(JsonInclude.Include.NON_ABSENT)
	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class PromptCapabilities {

		@JsonProperty("listChanged")
		private final Boolean listChanged;

		public PromptCapabilities(@JsonProperty("listChanged") Boolean listChanged) {
			this.listChanged = listChanged;
		}

		public Boolean getListChanged() {
			return listChanged;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o)
				return true;
			if (o == null || getClass() != o.getClass())
				return false;
			PromptCapabilities that = (PromptCapabilities) o;
			return Objects.equals(listChanged, that.listChanged);
		}

		@Override
		public int hashCode() {
			return Objects.hash(listChanged);
		}

		@Override
		public String toString() {
			return "PromptCapabilities{" + "listChanged=" + listChanged + '}';
		}

	}

	@JsonInclude(JsonInclude.Include.NON_ABSENT)
	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class ResourceCapabilities {

		@JsonProperty("subscribe")
		private final Boolean subscribe;

		@JsonProperty("listChanged")
		private final Boolean listChanged;

		public ResourceCapabilities(@JsonProperty("subscribe") Boolean subscribe,
				@JsonProperty("listChanged") Boolean listChanged) {
			this.subscribe = subscribe;
			this.listChanged = listChanged;
		}

		public Boolean getSubscribe() {
			return subscribe;
		}

		public Boolean getListChanged() {
			return listChanged;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o)
				return true;
			if (o == null || getClass() != o.getClass())
				return false;
			ResourceCapabilities that = (ResourceCapabilities) o;
			return Objects.equals(subscribe, that.subscribe) && Objects.equals(listChanged, that.listChanged);
		}

		@Override
		public int hashCode() {
			return Objects.hash(subscribe, listChanged);
		}

		@Override
		public String toString() {
			return "ResourceCapabilities{" + "subscribe=" + subscribe + ", listChanged=" + listChanged + '}';
		}

	}

	@JsonInclude(JsonInclude.Include.NON_ABSENT)
	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class ToolCapabilities {

		@JsonProperty("listChanged")
		private final Boolean listChanged;

		public ToolCapabilities(@JsonProperty("listChanged") Boolean listChanged) {
			this.listChanged = listChanged;
		}

		public Boolean getListChanged() {
			return listChanged;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o)
				return true;
			if (o == null || getClass() != o.getClass())
				return false;
			ToolCapabilities that = (ToolCapabilities) o;
			return Objects.equals(listChanged, that.listChanged);
		}

		@Override
		public int hashCode() {
			return Objects.hash(listChanged);
		}

		@Override
		public String toString() {
			return "ToolCapabilities{" + "listChanged=" + listChanged + '}';
		}

	}

	public static Builder builder() {
		return new Builder();
	}

	public static class Builder {

		private CompletionCapabilities completions;

		private Map<String, Object> experimental;

		private LoggingCapabilities logging = new LoggingCapabilities();

		private PromptCapabilities prompts;

		private ResourceCapabilities resources;

		private ToolCapabilities tools;

		public Builder completions() {
			this.completions = new CompletionCapabilities();
			return this;
		}

		public Builder experimental(Map<String, Object> experimental) {
			this.experimental = experimental;
			return this;
		}

		public Builder logging() {
			this.logging = new LoggingCapabilities();
			return this;
		}

		public Builder prompts(Boolean listChanged) {
			this.prompts = new PromptCapabilities(listChanged);
			return this;
		}

		public Builder resources(Boolean subscribe, Boolean listChanged) {
			this.resources = new ResourceCapabilities(subscribe, listChanged);
			return this;
		}

		public Builder tools(Boolean listChanged) {
			this.tools = new ToolCapabilities(listChanged);
			return this;
		}

		public ServerCapabilities build() {
			return new ServerCapabilities(completions, experimental, logging, prompts, resources, tools);
		}

	}

}
