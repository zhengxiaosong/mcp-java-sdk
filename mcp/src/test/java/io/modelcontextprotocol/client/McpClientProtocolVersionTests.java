/*
 * Copyright 2024-2024 the original author or authors.
 */

package io.modelcontextprotocol.client;

import java.time.Duration;
import java.util.List;

import io.modelcontextprotocol.MockMcpClientTransport;
import io.modelcontextprotocol.spec.McpError;
import io.modelcontextprotocol.spec.initialization.Implementation;
import io.modelcontextprotocol.spec.initialization.InitializeRequest;
import io.modelcontextprotocol.spec.initialization.InitializeResult;
import io.modelcontextprotocol.spec.jsonrpc.JSONRPCRequest;
import io.modelcontextprotocol.spec.jsonrpc.JSONRPCResponse;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static io.modelcontextprotocol.spec.McpSchema.JSONRPC_VERSION;
import static io.modelcontextprotocol.spec.McpSchema.LATEST_PROTOCOL_VERSION;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for MCP protocol version negotiation and compatibility.
 */
class McpClientProtocolVersionTests {

	private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(30);

	private static final Implementation CLIENT_INFO = new Implementation("test-client", "1.0.0");

	@Test
	void shouldUseLatestVersionByDefault() {
		MockMcpClientTransport transport = new MockMcpClientTransport();
		McpAsyncClient client = McpClient.async(transport)
			.clientInfo(CLIENT_INFO)
			.requestTimeout(REQUEST_TIMEOUT)
			.build();

		try {
			Mono<InitializeResult> initializeResultMono = client.initialize();

			StepVerifier.create(initializeResultMono).then(() -> {
				JSONRPCRequest request = transport.getLastSentMessageAsRequest();
				assertThat(request.getParams()).isInstanceOf(InitializeRequest.class);
				InitializeRequest initRequest = (InitializeRequest) request.getParams();
				assertThat(initRequest.getProtocolVersion()).isEqualTo(LATEST_PROTOCOL_VERSION);

				transport
					.simulateIncomingMessage(new JSONRPCResponse(JSONRPC_VERSION, request.getId(), new InitializeResult(
							LATEST_PROTOCOL_VERSION, null, new Implementation("test-server", "1.0.0"), null), null));
			}).assertNext(result -> {
				assertThat(result.getProtocolVersion()).isEqualTo(LATEST_PROTOCOL_VERSION);
			}).verifyComplete();

		}
		finally {
			// Ensure cleanup happens even if test fails
			StepVerifier.create(client.closeGracefully()).verifyComplete();
		}
	}

	@Test
	void shouldNegotiateSpecificVersion() {
		String oldVersion = "0.1.0";
		MockMcpClientTransport transport = new MockMcpClientTransport();
		McpAsyncClient client = McpClient.async(transport)
			.clientInfo(CLIENT_INFO)
			.requestTimeout(REQUEST_TIMEOUT)
			.build();

		client.setProtocolVersions(List.of(oldVersion, LATEST_PROTOCOL_VERSION));

		try {
			Mono<InitializeResult> initializeResultMono = client.initialize();

			StepVerifier.create(initializeResultMono).then(() -> {
				JSONRPCRequest request = transport.getLastSentMessageAsRequest();
				assertThat(request.getParams()).isInstanceOf(InitializeRequest.class);
				InitializeRequest initRequest = (InitializeRequest) request.getParams();
				assertThat(initRequest.getProtocolVersion()).isIn(List.of(oldVersion, LATEST_PROTOCOL_VERSION));

				transport.simulateIncomingMessage(new JSONRPCResponse(JSONRPC_VERSION, request.getId(),
						new InitializeResult(oldVersion, null, new Implementation("test-server", "1.0.0"), null),
						null));
			}).assertNext(result -> {
				assertThat(result.getProtocolVersion()).isEqualTo(oldVersion);
			}).verifyComplete();
		}
		finally {
			StepVerifier.create(client.closeGracefully()).verifyComplete();
		}
	}

	@Test
	void shouldFailForUnsupportedVersion() {
		String unsupportedVersion = "999.999.999";
		MockMcpClientTransport transport = new MockMcpClientTransport();
		McpAsyncClient client = McpClient.async(transport)
			.clientInfo(CLIENT_INFO)
			.requestTimeout(REQUEST_TIMEOUT)
			.build();

		try {
			Mono<InitializeResult> initializeResultMono = client.initialize();

			StepVerifier.create(initializeResultMono).then(() -> {
				JSONRPCRequest request = transport.getLastSentMessageAsRequest();
				assertThat(request.getParams()).isInstanceOf(InitializeRequest.class);

				transport.simulateIncomingMessage(
						new JSONRPCResponse(JSONRPC_VERSION, request.getId(), new InitializeResult(unsupportedVersion,
								null, new Implementation("test-server", "1.0.0"), null), null));
			}).expectError(McpError.class).verify();
		}
		finally {
			StepVerifier.create(client.closeGracefully()).verifyComplete();
		}
	}

	@Test
	void shouldUseHighestVersionWhenMultipleSupported() {
		String oldVersion = "0.1.0";
		String middleVersion = "0.2.0";
		String latestVersion = LATEST_PROTOCOL_VERSION;

		MockMcpClientTransport transport = new MockMcpClientTransport();
		McpAsyncClient client = McpClient.async(transport)
			.clientInfo(CLIENT_INFO)
			.requestTimeout(REQUEST_TIMEOUT)
			.build();

		client.setProtocolVersions(List.of(oldVersion, middleVersion, latestVersion));

		try {
			Mono<InitializeResult> initializeResultMono = client.initialize();

			StepVerifier.create(initializeResultMono).then(() -> {
				JSONRPCRequest request = transport.getLastSentMessageAsRequest();
				InitializeRequest initRequest = (InitializeRequest) request.getParams();
				assertThat(initRequest.getProtocolVersion()).isEqualTo(latestVersion);

				transport.simulateIncomingMessage(new JSONRPCResponse(JSONRPC_VERSION, request.getId(),
						new InitializeResult(latestVersion, null, new Implementation("test-server", "1.0.0"), null),
						null));
			}).assertNext(result -> {
				assertThat(result.getProtocolVersion()).isEqualTo(latestVersion);
			}).verifyComplete();
		}
		finally {
			StepVerifier.create(client.closeGracefully()).verifyComplete();
		}

	}

}
