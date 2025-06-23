package io.modelcontextprotocol.spec.initialization;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.modelcontextprotocol.spec.common.Request;
import java.util.Objects;

/**
 * 初始化请求。
 */
@JsonInclude(JsonInclude.Include.NON_ABSENT)
@JsonIgnoreProperties(ignoreUnknown = true)
public final class InitializeRequest implements Request {

	@JsonProperty("protocolVersion")
	private final String protocolVersion;

	@JsonProperty("capabilities")
	private final ClientCapabilities capabilities;

	@JsonProperty("clientInfo")
	private final Implementation clientInfo;

	public InitializeRequest(@JsonProperty("protocolVersion") String protocolVersion,
			@JsonProperty("capabilities") ClientCapabilities capabilities,
			@JsonProperty("clientInfo") Implementation clientInfo) {
		this.protocolVersion = protocolVersion;
		this.capabilities = capabilities;
		this.clientInfo = clientInfo;
	}

	public String getProtocolVersion() {
		return protocolVersion;
	}

	public ClientCapabilities getCapabilities() {
		return capabilities;
	}

	public Implementation getClientInfo() {
		return clientInfo;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		InitializeRequest that = (InitializeRequest) o;
		return Objects.equals(protocolVersion, that.protocolVersion) && Objects.equals(capabilities, that.capabilities)
				&& Objects.equals(clientInfo, that.clientInfo);
	}

	@Override
	public int hashCode() {
		return Objects.hash(protocolVersion, capabilities, clientInfo);
	}

	@Override
	public String toString() {
		return "InitializeRequest{" + "protocolVersion='" + protocolVersion + '\'' + ", capabilities=" + capabilities
				+ ", clientInfo=" + clientInfo + '}';
	}

}
