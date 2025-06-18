/*
 * Copyright 2024-2024 the original author or authors.
 */

package io.modelcontextprotocol;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.modelcontextprotocol.spec.McpSchema;
import io.modelcontextprotocol.spec.McpSchema.JSONRPCNotification;
import io.modelcontextprotocol.spec.McpSchema.JSONRPCRequest;
import io.modelcontextprotocol.spec.McpServerTransport;
import reactor.core.publisher.Mono;

/**
 * A mock implementation of the {@link McpServerTransport} interfaces.
 */
public class MockMcpServerTransport implements McpServerTransport {

	private final List<McpSchema.JSONRPCMessage> sent = new ArrayList<>();

	private final BiConsumer<MockMcpServerTransport, McpSchema.JSONRPCMessage> interceptor;

	public MockMcpServerTransport() {
		this((t, msg) -> {
		});
	}

	public MockMcpServerTransport(BiConsumer<MockMcpServerTransport, McpSchema.JSONRPCMessage> interceptor) {
		this.interceptor = interceptor;
	}

	@Override
	public Mono<Void> sendMessage(McpSchema.JSONRPCMessage message) {
		sent.add(message);
		interceptor.accept(this, message);
		return Mono.empty();
	}

	public McpSchema.JSONRPCRequest getLastSentMessageAsRequest() {
		return (JSONRPCRequest) getLastSentMessage();
	}

	public McpSchema.JSONRPCNotification getLastSentMessageAsNotification() {
		return (JSONRPCNotification) getLastSentMessage();
	}

	public McpSchema.JSONRPCMessage getLastSentMessage() {
		return !sent.isEmpty() ? sent.get(sent.size() - 1) : null;
	}

	@Override
	public Mono<Void> closeGracefully() {
		return Mono.empty();
	}

	@Override
	public <T> T unmarshalFrom(Object data, TypeReference<T> typeRef) {
		return new ObjectMapper().convertValue(data, typeRef);
	}

}
