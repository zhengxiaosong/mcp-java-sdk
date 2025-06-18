/*
 * Copyright 2024-2024 the original author or authors.
 */

package io.modelcontextprotocol.client;

import java.time.Duration;

import io.modelcontextprotocol.client.transport.WebFluxSseClientTransport;
import io.modelcontextprotocol.spec.McpClientTransport;
import org.junit.jupiter.api.Timeout;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;

import org.springframework.web.reactive.function.client.WebClient;

/**
 * Tests for the {@link McpSyncClient} with {@link WebFluxSseClientTransport}.
 *
 * @author Christian Tzolov
 */
@Timeout(15) // Giving extra time beyond the client timeout
class WebFluxSseMcpSyncClientTests extends AbstractMcpSyncClientTests {

	static String host = "http://localhost:3001";

	// Uses the https://github.com/tzolov/mcp-everything-server-docker-image
	@SuppressWarnings("resource")
	GenericContainer<?> container = new GenericContainer<>("docker.io/tzolov/mcp-everything-server:v1")
		.withLogConsumer(outputFrame -> System.out.println(outputFrame.getUtf8String()))
		.withExposedPorts(3001)
		.waitingFor(Wait.forHttp("/").forStatusCode(404));

	@Override
	protected McpClientTransport createMcpTransport() {
		return WebFluxSseClientTransport.builder(WebClient.builder().baseUrl(host)).build();
	}

	@Override
	protected void onStart() {
		container.start();
		int port = container.getMappedPort(3001);
		host = "http://" + container.getHost() + ":" + port;
	}

	@Override
	protected void onClose() {
		container.stop();
	}

	protected Duration getInitializationTimeout() {
		return Duration.ofSeconds(1);
	}

}
