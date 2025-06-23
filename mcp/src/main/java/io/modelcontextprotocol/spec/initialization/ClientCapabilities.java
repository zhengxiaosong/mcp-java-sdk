package io.modelcontextprotocol.spec.initialization;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;
import java.util.Objects;

/**
 * 客户端能力声明。
 */
@JsonInclude(JsonInclude.Include.NON_ABSENT)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ClientCapabilities {

	@JsonProperty("experimental")
	private final Map<String, Object> experimental;

	@JsonProperty("roots")
	private final RootCapabilities roots;

	@JsonProperty("sampling")
	private final Sampling sampling;

	public ClientCapabilities(@JsonProperty("experimental") Map<String, Object> experimental,
			@JsonProperty("roots") RootCapabilities roots, @JsonProperty("sampling") Sampling sampling) {
		this.experimental = experimental;
		this.roots = roots;
		this.sampling = sampling;
	}

	public Map<String, Object> getExperimental() {
		return experimental;
	}

	public RootCapabilities getRoots() {
		return roots;
	}

	public Sampling getSampling() {
		return sampling;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		ClientCapabilities that = (ClientCapabilities) o;
		return Objects.equals(experimental, that.experimental) && Objects.equals(roots, that.roots)
				&& Objects.equals(sampling, that.sampling);
	}

	@Override
	public int hashCode() {
		return Objects.hash(experimental, roots, sampling);
	}

	@Override
	public String toString() {
		return "ClientCapabilities{" + "experimental=" + experimental + ", roots=" + roots + ", sampling=" + sampling
				+ '}';
	}

	@JsonInclude(JsonInclude.Include.NON_ABSENT)
	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class RootCapabilities {

		@JsonProperty("listChanged")
		private final Boolean listChanged;

		public RootCapabilities(@JsonProperty("listChanged") Boolean listChanged) {
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
			RootCapabilities that = (RootCapabilities) o;
			return Objects.equals(listChanged, that.listChanged);
		}

		@Override
		public int hashCode() {
			return Objects.hash(listChanged);
		}

		@Override
		public String toString() {
			return "RootCapabilities{" + "listChanged=" + listChanged + '}';
		}

	}

	@JsonInclude(JsonInclude.Include.NON_ABSENT)
	public static class Sampling {

		public Sampling() {
		}

		@Override
		public boolean equals(Object o) {
			return o instanceof Sampling;
		}

		@Override
		public int hashCode() {
			return 0;
		}

		@Override
		public String toString() {
			return "Sampling{}";
		}

	}

	public static class Builder {

		private Map<String, Object> experimental;

		private RootCapabilities roots;

		private Sampling sampling;

		public Builder experimental(Map<String, Object> experimental) {
			this.experimental = experimental;
			return this;
		}

		public Builder roots(Boolean listChanged) {
			this.roots = new RootCapabilities(listChanged);
			return this;
		}

		public Builder sampling() {
			this.sampling = new Sampling();
			return this;
		}

		public ClientCapabilities build() {
			return new ClientCapabilities(experimental, roots, sampling);
		}

	}

	public static Builder builder() {
		return new Builder();
	}

}
