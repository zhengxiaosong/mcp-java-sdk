/*
 * Copyright 2024-2024 the original author or authors.
 */

package io.modelcontextprotocol.server;

import java.util.List;
import java.util.UUID;

import io.modelcontextprotocol.MockMcpServerTransport;
import io.modelcontextprotocol.MockMcpServerTransportProvider;
import io.modelcontextprotocol.spec.McpSchema;
import io.modelcontextprotocol.spec.initialization.Implementation;
import io.modelcontextprotocol.spec.initialization.InitializeRequest;
import io.modelcontextprotocol.spec.initialization.InitializeResult;
import io.modelcontextprotocol.spec.jsonrpc.JSONRPCMessage;
import io.modelcontextprotocol.spec.jsonrpc.JSONRPCRequest;
import io.modelcontextprotocol.spec.jsonrpc.JSONRPCResponse;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for MCP server protocol version negotiation and compatibility.
 */
class McpServerProtocolVersionTests {

	private static final Implementation SERVER_INFO = new Implementation("test-server", "1.0.0");

	private static final Implementation CLIENT_INFO = new Implementation("test-client", "1.0.0");

	private JSONRPCRequest jsonRpcInitializeRequest(String requestId, String protocolVersion) {
		return new JSONRPCRequest(McpSchema.JSONRPC_VERSION, McpSchema.METHOD_INITIALIZE, requestId,
				new InitializeRequest(protocolVersion, null, CLIENT_INFO));
	}

	@Test
	void shouldUseLatestVersionByDefault() {
		MockMcpServerTransport serverTransport = new MockMcpServerTransport();
		var transportProvider = new MockMcpServerTransportProvider(serverTransport);
		McpAsyncServer server = McpServer.async(transportProvider).serverInfo(SERVER_INFO).build();

		String requestId = UUID.randomUUID().toString();

		transportProvider
			.simulateIncomingMessage(jsonRpcInitializeRequest(requestId, McpSchema.LATEST_PROTOCOL_VERSION));

		JSONRPCMessage response = serverTransport.getLastSentMessage();
		assertThat(response).isInstanceOf(JSONRPCResponse.class);
		JSONRPCResponse jsonResponse = (JSONRPCResponse) response;
		assertThat(jsonResponse.getId()).isEqualTo(requestId);
		assertThat(jsonResponse.getResult()).isInstanceOf(InitializeResult.class);
		InitializeResult result = (InitializeResult) jsonResponse.getResult();
		assertThat(result.getProtocolVersion()).isEqualTo(McpSchema.LATEST_PROTOCOL_VERSION);

		server.closeGracefully().subscribe();
	}

	@Test
	void shouldNegotiateSpecificVersion() {
		String oldVersion = "0.1.0";
		MockMcpServerTransport serverTransport = new MockMcpServerTransport();
		var transportProvider = new MockMcpServerTransportProvider(serverTransport);

		McpAsyncServer server = McpServer.async(transportProvider).serverInfo(SERVER_INFO).build();

		server.setProtocolVersions(List.of(oldVersion, McpSchema.LATEST_PROTOCOL_VERSION));

		String requestId = UUID.randomUUID().toString();

		transportProvider.simulateIncomingMessage(jsonRpcInitializeRequest(requestId, oldVersion));

		JSONRPCMessage response = serverTransport.getLastSentMessage();
		assertThat(response).isInstanceOf(JSONRPCResponse.class);
		JSONRPCResponse jsonResponse = (JSONRPCResponse) response;
		assertThat(jsonResponse.getId()).isEqualTo(requestId);
		assertThat(jsonResponse.getResult()).isInstanceOf(InitializeResult.class);
		InitializeResult result = (InitializeResult) jsonResponse.getResult();
		assertThat(result.getProtocolVersion()).isEqualTo(oldVersion);

		server.closeGracefully().subscribe();
	}

	@Test
	void shouldSuggestLatestVersionForUnsupportedVersion() {
		String unsupportedVersion = "999.999.999";
		MockMcpServerTransport serverTransport = new MockMcpServerTransport();
		var transportProvider = new MockMcpServerTransportProvider(serverTransport);

		McpAsyncServer server = McpServer.async(transportProvider).serverInfo(SERVER_INFO).build();

		String requestId = UUID.randomUUID().toString();

		transportProvider.simulateIncomingMessage(jsonRpcInitializeRequest(requestId, unsupportedVersion));

		JSONRPCMessage response = serverTransport.getLastSentMessage();
		assertThat(response).isInstanceOf(JSONRPCResponse.class);
		JSONRPCResponse jsonResponse = (JSONRPCResponse) response;
		assertThat(jsonResponse.getId()).isEqualTo(requestId);
		assertThat(jsonResponse.getResult()).isInstanceOf(InitializeResult.class);
		InitializeResult result = (InitializeResult) jsonResponse.getResult();
		assertThat(result.getProtocolVersion()).isEqualTo(McpSchema.LATEST_PROTOCOL_VERSION);

		server.closeGracefully().subscribe();
	}

	@Test
	void shouldUseHighestVersionWhenMultipleSupported() {
		String oldVersion = "0.1.0";
		String middleVersion = "0.2.0";
		String latestVersion = McpSchema.LATEST_PROTOCOL_VERSION;

		MockMcpServerTransport serverTransport = new MockMcpServerTransport();
		var transportProvider = new MockMcpServerTransportProvider(serverTransport);

		McpAsyncServer server = McpServer.async(transportProvider).serverInfo(SERVER_INFO).build();

		server.setProtocolVersions(List.of(oldVersion, middleVersion, latestVersion));

		String requestId = UUID.randomUUID().toString();
		transportProvider.simulateIncomingMessage(jsonRpcInitializeRequest(requestId, latestVersion));

		JSONRPCMessage response = serverTransport.getLastSentMessage();
		assertThat(response).isInstanceOf(JSONRPCResponse.class);
		JSONRPCResponse jsonResponse = (JSONRPCResponse) response;
		assertThat(jsonResponse.getId()).isEqualTo(requestId);
		assertThat(jsonResponse.getResult()).isInstanceOf(InitializeResult.class);
		InitializeResult result = (InitializeResult) jsonResponse.getResult();
		assertThat(result.getProtocolVersion()).isEqualTo(latestVersion);

		server.closeGracefully().subscribe();
	}

}
