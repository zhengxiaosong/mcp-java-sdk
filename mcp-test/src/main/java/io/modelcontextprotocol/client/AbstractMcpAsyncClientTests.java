/*
 * Copyright 2024-2024 the original author or authors.
 */

package io.modelcontextprotocol.client;

import java.time.Duration;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Function;

import io.modelcontextprotocol.spec.McpClientTransport;
import io.modelcontextprotocol.spec.McpError;
import io.modelcontextprotocol.spec.common.Root;
import io.modelcontextprotocol.spec.initialization.ClientCapabilities;
import io.modelcontextprotocol.spec.logging.LoggingLevel;
import io.modelcontextprotocol.spec.prompt.GetPromptRequest;
import io.modelcontextprotocol.spec.prompt.Prompt;
import io.modelcontextprotocol.spec.resource.Resource;
import io.modelcontextprotocol.spec.resource.SubscribeRequest;
import io.modelcontextprotocol.spec.resource.UnsubscribeRequest;
import io.modelcontextprotocol.spec.sampling.CreateMessageRequest;
import io.modelcontextprotocol.spec.sampling.CreateMessageResult;
import io.modelcontextprotocol.spec.tool.CallToolRequest;
import io.modelcontextprotocol.spec.tool.Tool;
import io.modelcontextprotocol.spec.McpTransport;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Test suite for the {@link McpAsyncClient} that can be used with different
 * {@link McpTransport} implementations.
 *
 * @author Christian Tzolov
 * @author Dariusz Jędrzejczyk
 */
public abstract class AbstractMcpAsyncClientTests {

	private static final String ECHO_TEST_MESSAGE = "Hello MCP Spring AI!";

	abstract protected McpClientTransport createMcpTransport();

	protected void onStart() {
	}

	protected void onClose() {
	}

	protected Duration getRequestTimeout() {
		return Duration.ofSeconds(14);
	}

	protected Duration getInitializationTimeout() {
		return Duration.ofSeconds(2);
	}

	McpAsyncClient client(McpClientTransport transport) {
		return client(transport, Function.identity());
	}

	McpAsyncClient client(McpClientTransport transport, Function<McpClient.AsyncSpec, McpClient.AsyncSpec> customizer) {
		AtomicReference<McpAsyncClient> client = new AtomicReference<>();

		assertThatCode(() -> {
			McpClient.AsyncSpec builder = McpClient.async(transport)
				.requestTimeout(getRequestTimeout())
				.initializationTimeout(getInitializationTimeout())
				.capabilities(ClientCapabilities.builder().roots(true).build());
			builder = customizer.apply(builder);
			client.set(builder.build());
		}).doesNotThrowAnyException();

		return client.get();
	}

	void withClient(McpClientTransport transport, Consumer<McpAsyncClient> c) {
		withClient(transport, Function.identity(), c);
	}

	void withClient(McpClientTransport transport, Function<McpClient.AsyncSpec, McpClient.AsyncSpec> customizer,
			Consumer<McpAsyncClient> c) {
		var client = client(transport, customizer);
		try {
			c.accept(client);
		}
		finally {
			StepVerifier.create(client.closeGracefully()).expectComplete().verify(Duration.ofSeconds(10));
		}
	}

	@BeforeEach
	void setUp() {
		onStart();
	}

	@AfterEach
	void tearDown() {
		onClose();
	}

	<T> void verifyInitializationTimeout(Function<McpAsyncClient, Mono<T>> operation, String action) {
		withClient(createMcpTransport(), mcpAsyncClient -> {
			StepVerifier.<T>withVirtualTime(() -> operation.apply(mcpAsyncClient))
				.expectSubscription()
				.thenAwait(getInitializationTimeout())
				.consumeErrorWith(e -> assertThat(e).isInstanceOf(McpError.class)
					.hasMessage("Client must be initialized before " + action))
				.verify();
		});
	}

	@Test
	void testConstructorWithInvalidArguments() {
		assertThatThrownBy(() -> McpClient.async(null).build()).isInstanceOf(IllegalArgumentException.class)
			.hasMessage("Transport must not be null");

		assertThatThrownBy(() -> McpClient.async(createMcpTransport()).requestTimeout(null).build())
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessage("Request timeout must not be null");
	}

	@Test
	void testListToolsWithoutInitialization() {
		verifyInitializationTimeout(client -> client.listTools(null), "listing tools");
	}

	@Test
	void testListTools() {
		withClient(createMcpTransport(), mcpAsyncClient -> {
			StepVerifier.create(mcpAsyncClient.initialize().then(mcpAsyncClient.listTools(null)))
				.consumeNextWith(result -> {
					assertThat(result.getTools()).isNotNull().isNotEmpty();

					Tool firstTool = result.getTools().get(0);
					assertThat(firstTool.getName()).isNotNull();
					assertThat(firstTool.getDescription()).isNotNull();
				})
				.verifyComplete();
		});
	}

	@Test
	void testPingWithoutInitialization() {
		verifyInitializationTimeout(client -> client.ping(), "pinging the server");
	}

	@Test
	void testPing() {
		withClient(createMcpTransport(), mcpAsyncClient -> {
			StepVerifier.create(mcpAsyncClient.initialize().then(mcpAsyncClient.ping()))
				.expectNextCount(1)
				.verifyComplete();
		});
	}

	@Test
	void testCallToolWithoutInitialization() {
		CallToolRequest callToolRequest = new CallToolRequest("echo", Map.of("message", ECHO_TEST_MESSAGE));
		verifyInitializationTimeout(client -> client.callTool(callToolRequest), "calling tools");
	}

	@Test
	void testCallTool() {
		withClient(createMcpTransport(), mcpAsyncClient -> {
			CallToolRequest callToolRequest = new CallToolRequest("echo", Map.of("message", ECHO_TEST_MESSAGE));

			StepVerifier.create(mcpAsyncClient.initialize().then(mcpAsyncClient.callTool(callToolRequest)))
				.consumeNextWith(callToolResult -> {
					assertThat(callToolResult).isNotNull().satisfies(result -> {
						assertThat(result.getContent()).isNotNull();
						assertThat(result.isError()).isNull();
					});
				})
				.verifyComplete();
		});
	}

	@Test
	void testCallToolWithInvalidTool() {
		withClient(createMcpTransport(), mcpAsyncClient -> {
			CallToolRequest invalidRequest = new CallToolRequest("nonexistent_tool",
					Map.of("message", ECHO_TEST_MESSAGE));

			StepVerifier.create(mcpAsyncClient.initialize().then(mcpAsyncClient.callTool(invalidRequest)))
				.consumeErrorWith(
						e -> assertThat(e).isInstanceOf(McpError.class).hasMessage("Unknown tool: nonexistent_tool"))
				.verify();
		});
	}

	@Test
	void testListResourcesWithoutInitialization() {
		verifyInitializationTimeout(client -> client.listResources(null), "listing resources");
	}

	@Test
	void testListResources() {
		withClient(createMcpTransport(), mcpAsyncClient -> {
			StepVerifier.create(mcpAsyncClient.initialize().then(mcpAsyncClient.listResources(null)))
				.consumeNextWith(resources -> {
					assertThat(resources).isNotNull().satisfies(result -> {
						assertThat(result.getResources()).isNotNull();

						if (!result.getResources().isEmpty()) {
							Resource firstResource = result.getResources().get(0);
							assertThat(firstResource.getUri()).isNotNull();
							assertThat(firstResource.getName()).isNotNull();
						}
					});
				})
				.verifyComplete();
		});
	}

	@Test
	void testMcpAsyncClientState() {
		withClient(createMcpTransport(), mcpAsyncClient -> {
			assertThat(mcpAsyncClient).isNotNull();
		});
	}

	@Test
	void testListPromptsWithoutInitialization() {
		verifyInitializationTimeout(client -> client.listPrompts(null), "listing " + "prompts");
	}

	@Test
	void testListPrompts() {
		withClient(createMcpTransport(), mcpAsyncClient -> {
			StepVerifier.create(mcpAsyncClient.initialize().then(mcpAsyncClient.listPrompts(null)))
				.consumeNextWith(prompts -> {
					assertThat(prompts).isNotNull().satisfies(result -> {
						assertThat(result.getPrompts()).isNotNull();

						if (!result.getPrompts().isEmpty()) {
							Prompt firstPrompt = result.getPrompts().get(0);
							assertThat(firstPrompt.getName()).isNotNull();
							assertThat(firstPrompt.getDescription()).isNotNull();
						}
					});
				})
				.verifyComplete();
		});
	}

	@Test
	void testGetPromptWithoutInitialization() {
		GetPromptRequest request = new GetPromptRequest("simple_prompt", Map.of());
		verifyInitializationTimeout(client -> client.getPrompt(request), "getting " + "prompts");
	}

	@Test
	void testGetPrompt() {
		withClient(createMcpTransport(), mcpAsyncClient -> {
			StepVerifier
				.create(mcpAsyncClient.initialize()
					.then(mcpAsyncClient.getPrompt(new GetPromptRequest("simple_prompt", Map.of()))))
				.consumeNextWith(prompt -> {
					assertThat(prompt).isNotNull().satisfies(result -> {
						assertThat(result.getMessages()).isNotEmpty();
						assertThat(result.getMessages()).hasSize(1);
					});
				})
				.verifyComplete();
		});
	}

	@Test
	void testRootsListChangedWithoutInitialization() {
		verifyInitializationTimeout(client -> client.rootsListChangedNotification(),
				"sending roots list changed notification");
	}

	@Test
	void testRootsListChanged() {
		withClient(createMcpTransport(), mcpAsyncClient -> {
			StepVerifier.create(mcpAsyncClient.initialize().then(mcpAsyncClient.rootsListChangedNotification()))
				.verifyComplete();
		});
	}

	@Test
	void testInitializeWithRootsListProviders() {
		withClient(createMcpTransport(), builder -> builder.roots(new Root("file:///test/path", "test-root")),
				client -> {
					StepVerifier.create(client.initialize().then(client.closeGracefully())).verifyComplete();
				});
	}

	@Test
	void testAddRoot() {
		withClient(createMcpTransport(), mcpAsyncClient -> {
			Root newRoot = new Root("file:///new/test/path", "new-test-root");
			StepVerifier.create(mcpAsyncClient.addRoot(newRoot)).verifyComplete();
		});
	}

	@Test
	void testAddRootWithNullValue() {
		withClient(createMcpTransport(), mcpAsyncClient -> {
			StepVerifier.create(mcpAsyncClient.addRoot(null))
				.consumeErrorWith(e -> assertThat(e).isInstanceOf(McpError.class).hasMessage("Root must not be null"))
				.verify();
		});
	}

	@Test
	void testRemoveRoot() {
		withClient(createMcpTransport(), mcpAsyncClient -> {
			Root root = new Root("file:///test/path/to/remove", "root-to-remove");
			StepVerifier.create(mcpAsyncClient.addRoot(root)).verifyComplete();

			StepVerifier.create(mcpAsyncClient.removeRoot(root.getUri())).verifyComplete();
		});
	}

	@Test
	void testRemoveNonExistentRoot() {
		withClient(createMcpTransport(), mcpAsyncClient -> {
			StepVerifier.create(mcpAsyncClient.removeRoot("nonexistent-uri"))
				.consumeErrorWith(e -> assertThat(e).isInstanceOf(McpError.class)
					.hasMessage("Root with uri 'nonexistent-uri' not found"))
				.verify();
		});
	}

	@Test
	@Disabled
	void testReadResource() {
		withClient(createMcpTransport(), mcpAsyncClient -> {
			StepVerifier.create(mcpAsyncClient.listResources()).consumeNextWith(resources -> {
				if (!resources.getResources().isEmpty()) {
					Resource firstResource = resources.getResources().get(0);
					StepVerifier.create(mcpAsyncClient.readResource(firstResource)).consumeNextWith(result -> {
						assertThat(result).isNotNull();
						assertThat(result.getContents()).isNotNull();
					}).verifyComplete();
				}
			}).verifyComplete();
		});
	}

	@Test
	void testListResourceTemplatesWithoutInitialization() {
		verifyInitializationTimeout(client -> client.listResourceTemplates(), "listing resource templates");
	}

	@Test
	void testListResourceTemplates() {
		withClient(createMcpTransport(), mcpAsyncClient -> {
			StepVerifier.create(mcpAsyncClient.initialize().then(mcpAsyncClient.listResourceTemplates()))
				.consumeNextWith(result -> {
					assertThat(result).isNotNull();
					assertThat(result.getResourceTemplates()).isNotNull();
				})
				.verifyComplete();
		});
	}

	// @Test
	void testResourceSubscription() {
		withClient(createMcpTransport(), mcpAsyncClient -> {
			StepVerifier.create(mcpAsyncClient.listResources()).consumeNextWith(resources -> {
				if (!resources.getResources().isEmpty()) {
					Resource firstResource = resources.getResources().get(0);

					// Test subscribe
					StepVerifier.create(mcpAsyncClient.subscribeResource(new SubscribeRequest(firstResource.getUri())))
						.verifyComplete();

					// Test unsubscribe
					StepVerifier
						.create(mcpAsyncClient.unsubscribeResource(new UnsubscribeRequest(firstResource.getUri())))
						.verifyComplete();
				}
			}).verifyComplete();
		});
	}

	@Test
	void testNotificationHandlers() {
		AtomicBoolean toolsNotificationReceived = new AtomicBoolean(false);
		AtomicBoolean resourcesNotificationReceived = new AtomicBoolean(false);
		AtomicBoolean promptsNotificationReceived = new AtomicBoolean(false);

		withClient(createMcpTransport(),
				builder -> builder
					.toolsChangeConsumer(tools -> Mono.fromRunnable(() -> toolsNotificationReceived.set(true)))
					.resourcesChangeConsumer(
							resources -> Mono.fromRunnable(() -> resourcesNotificationReceived.set(true)))
					.promptsChangeConsumer(prompts -> Mono.fromRunnable(() -> promptsNotificationReceived.set(true))),
				mcpAsyncClient -> {
					StepVerifier.create(mcpAsyncClient.initialize())
						.expectNextMatches(Objects::nonNull)
						.verifyComplete();
				});
	}

	@Test
	void testInitializeWithSamplingCapability() {
		ClientCapabilities capabilities = ClientCapabilities.builder().sampling().build();
		CreateMessageResult createMessageResult = CreateMessageResult.builder()
			.message("test")
			.model("test-model")
			.build();
		withClient(createMcpTransport(),
				builder -> builder.capabilities(capabilities).sampling(request -> Mono.just(createMessageResult)),
				client -> {
					StepVerifier.create(client.initialize()).expectNextMatches(Objects::nonNull).verifyComplete();
				});
	}

	@Test
	void testInitializeWithAllCapabilities() {
		var capabilities = ClientCapabilities.builder()
			.experimental(Map.of("feature", "test"))
			.roots(true)
			.sampling()
			.build();

		Function<CreateMessageRequest, Mono<CreateMessageResult>> samplingHandler = request -> Mono
			.just(CreateMessageResult.builder().message("test").model("test-model").build());

		withClient(createMcpTransport(), builder -> builder.capabilities(capabilities).sampling(samplingHandler),
				client ->

				StepVerifier.create(client.initialize()).assertNext(result -> {
					assertThat(result).isNotNull();
					assertThat(result.getCapabilities()).isNotNull();
				}).verifyComplete());
	}

	// ---------------------------------------
	// Logging Tests
	// ---------------------------------------

	@Test
	void testLoggingLevelsWithoutInitialization() {
		verifyInitializationTimeout(client -> client.setLoggingLevel(LoggingLevel.DEBUG), "setting logging level");
	}

	@Test
	void testLoggingLevels() {
		withClient(createMcpTransport(), mcpAsyncClient -> {
			StepVerifier
				.create(mcpAsyncClient.initialize()
					.thenMany(Flux.fromArray(LoggingLevel.values()).flatMap(mcpAsyncClient::setLoggingLevel)))
				.verifyComplete();
		});
	}

	@Test
	void testLoggingConsumer() {
		AtomicBoolean logReceived = new AtomicBoolean(false);

		withClient(createMcpTransport(),
				builder -> builder.loggingConsumer(notification -> Mono.fromRunnable(() -> logReceived.set(true))),
				client -> {
					StepVerifier.create(client.initialize()).expectNextMatches(Objects::nonNull).verifyComplete();
					StepVerifier.create(client.closeGracefully()).verifyComplete();

				});

	}

	@Test
	void testLoggingWithNullNotification() {
		withClient(createMcpTransport(), mcpAsyncClient -> {
			StepVerifier.create(mcpAsyncClient.setLoggingLevel(null))
				.expectErrorMatches(error -> error.getMessage().contains("Logging level must not be null"))
				.verify();
		});
	}

}
