package io.modelcontextprotocol.spec.initialization;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;

/**
 * 初始化响应。
 */
@JsonInclude(JsonInclude.Include.NON_ABSENT)
@JsonIgnoreProperties(ignoreUnknown = true)
public class InitializeResult {

	@JsonProperty("protocolVersion")
	private final String protocolVersion;

	@JsonProperty("capabilities")
	private final ServerCapabilities capabilities;

	@JsonProperty("serverInfo")
	private final Implementation serverInfo;

	@JsonProperty("instructions")
	private final String instructions;

	public InitializeResult(@JsonProperty("protocolVersion") String protocolVersion,
			@JsonProperty("capabilities") ServerCapabilities capabilities,
			@JsonProperty("serverInfo") Implementation serverInfo, @JsonProperty("instructions") String instructions) {
		this.protocolVersion = protocolVersion;
		this.capabilities = capabilities;
		this.serverInfo = serverInfo;
		this.instructions = instructions;
	}

	public String getProtocolVersion() {
		return protocolVersion;
	}

	public ServerCapabilities getCapabilities() {
		return capabilities;
	}

	public Implementation getServerInfo() {
		return serverInfo;
	}

	public String getInstructions() {
		return instructions;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		InitializeResult that = (InitializeResult) o;
		return Objects.equals(protocolVersion, that.protocolVersion) && Objects.equals(capabilities, that.capabilities)
				&& Objects.equals(serverInfo, that.serverInfo) && Objects.equals(instructions, that.instructions);
	}

	@Override
	public int hashCode() {
		return Objects.hash(protocolVersion, capabilities, serverInfo, instructions);
	}

	@Override
	public String toString() {
		return "InitializeResult{" + "protocolVersion='" + protocolVersion + '\'' + ", capabilities=" + capabilities
				+ ", serverInfo=" + serverInfo + ", instructions='" + instructions + '\'' + '}';
	}

}
