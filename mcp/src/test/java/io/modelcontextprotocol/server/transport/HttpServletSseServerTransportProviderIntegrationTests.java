/*
 * Copyright 2024 - 2024 the original author or authors.
 */
package io.modelcontextprotocol.server.transport;

import java.net.URI;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.modelcontextprotocol.client.McpClient;
import io.modelcontextprotocol.client.transport.HttpClientSseClientTransport;
import io.modelcontextprotocol.server.McpServer;
import io.modelcontextprotocol.server.McpServerFeatures;
import io.modelcontextprotocol.spec.McpError;
import io.modelcontextprotocol.spec.common.Role;
import io.modelcontextprotocol.spec.common.Root;
import io.modelcontextprotocol.spec.content.TextContent;
import io.modelcontextprotocol.spec.initialization.ClientCapabilities;
import io.modelcontextprotocol.spec.initialization.Implementation;
import io.modelcontextprotocol.spec.initialization.InitializeResult;
import io.modelcontextprotocol.spec.initialization.ServerCapabilities;
import io.modelcontextprotocol.spec.jsonrpc.JSONRPCRequest;
import io.modelcontextprotocol.spec.jsonrpc.JSONRPCResponse;
import io.modelcontextprotocol.spec.logging.LoggingLevel;
import io.modelcontextprotocol.spec.logging.LoggingMessageNotification;
import io.modelcontextprotocol.spec.sampling.CreateMessageRequest;
import io.modelcontextprotocol.spec.sampling.CreateMessageResult;
import io.modelcontextprotocol.spec.sampling.ModelPreferences;
import io.modelcontextprotocol.spec.sampling.SamplingMessage;
import io.modelcontextprotocol.spec.tool.CallToolRequest;
import io.modelcontextprotocol.spec.tool.CallToolResult;
import io.modelcontextprotocol.spec.tool.Tool;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.LifecycleState;
import org.apache.catalina.startup.Tomcat;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.awaitility.Awaitility.await;
import static org.mockito.Mockito.mock;

class HttpServletSseServerTransportProviderIntegrationTests {

	private static final int PORT = TomcatTestUtil.findAvailablePort();

	private static final String CUSTOM_SSE_ENDPOINT = "/somePath/sse";

	private static final String CUSTOM_MESSAGE_ENDPOINT = "/otherPath/mcp/message";

	private HttpServletSseServerTransportProvider mcpServerTransportProvider;

	McpClient.SyncSpec clientBuilder;

	private Tomcat tomcat;

	private RestTemplate restTemplate;

	private ObjectMapper objectMapper;

	@BeforeEach
	public void before() {
		// Create and configure the transport provider
		mcpServerTransportProvider = HttpServletSseServerTransportProvider.builder()
			.objectMapper(new ObjectMapper())
			.messageEndpoint(CUSTOM_MESSAGE_ENDPOINT)
			.sseEndpoint(CUSTOM_SSE_ENDPOINT)
			.build();

		tomcat = TomcatTestUtil.createTomcatServer("", PORT, mcpServerTransportProvider);
		try {
			tomcat.start();
			assertThat(tomcat.getServer().getState()).isEqualTo(LifecycleState.STARTED);
		}
		catch (Exception e) {
			throw new RuntimeException("Failed to start Tomcat", e);
		}

		this.clientBuilder = McpClient.sync(HttpClientSseClientTransport.builder("http://localhost:" + PORT)
			.sseEndpoint(CUSTOM_SSE_ENDPOINT)
			.build());

		this.restTemplate = new RestTemplate();
		this.objectMapper = new ObjectMapper();
	}

	@AfterEach
	public void after() {
		if (mcpServerTransportProvider != null) {
			mcpServerTransportProvider.closeGracefully().block();
		}
		if (tomcat != null) {
			try {
				tomcat.stop();
				tomcat.destroy();
			}
			catch (LifecycleException e) {
				throw new RuntimeException("Failed to stop Tomcat", e);
			}
		}
	}

	// ---------------------------------------
	// Sampling Tests
	// ---------------------------------------
	@Test
	@Disabled
	void testCreateMessageWithoutSamplingCapabilities() {

		McpServerFeatures.AsyncToolSpecification tool = new McpServerFeatures.AsyncToolSpecification(
				new Tool("tool1", "tool1 description", emptyJsonSchema), (exchange, request) -> {

					exchange.createMessage(mock(CreateMessageRequest.class)).block();

					return Mono.just(mock(CallToolResult.class));
				});

		var server = McpServer.async(mcpServerTransportProvider).serverInfo("test-server", "1.0.0").tools(tool).build();

		try (
				// Create client without sampling capabilities
				var client = clientBuilder.clientInfo(new Implementation("Sample " + "client", "0.0.0")).build()) {

			assertThat(client.initialize()).isNotNull();

			try {
				client.callTool(new CallToolRequest("tool1", Map.of()));
			}
			catch (McpError e) {
				assertThat(e).isInstanceOf(McpError.class)
					.hasMessage("Client must be configured with sampling capabilities");
			}
		}
		server.close();
	}

	@Test
	void testCreateMessageSuccess() {

		Function<CreateMessageRequest, CreateMessageResult> samplingHandler = request -> {
			assertThat(request.getMessages()).hasSize(1);
			assertThat(request.getMessages().get(0).getContent()).isInstanceOf(TextContent.class);

			return new CreateMessageResult(Role.USER, new TextContent("Test message"), "MockModelName",
					CreateMessageResult.StopReason.STOP_SEQUENCE);
		};

		CallToolResult callResponse = new CallToolResult(List.of(new TextContent("CALL RESPONSE")), false);

		McpServerFeatures.AsyncToolSpecification tool = new McpServerFeatures.AsyncToolSpecification(
				new Tool("tool1", "tool1 description", emptyJsonSchema), (exchange, request) -> {

					var createMessageRequest = CreateMessageRequest.builder()
						.messages(List.of(new SamplingMessage(Role.USER, new TextContent("Test message"))))
						.modelPreferences(ModelPreferences.builder().build())
						.build();

					StepVerifier.create(exchange.createMessage(createMessageRequest)).consumeNextWith(result -> {
						assertThat(result).isNotNull();
						assertThat(result.getRole()).isEqualTo(Role.USER);
						assertThat(result.getContent()).isInstanceOf(TextContent.class);
						assertThat(((TextContent) result.getContent()).getText()).isEqualTo("Test message");
						assertThat(result.getModel()).isEqualTo("MockModelName");
						assertThat(result.getStopReason()).isEqualTo(CreateMessageResult.StopReason.STOP_SEQUENCE);
					}).verifyComplete();

					return Mono.just(callResponse);
				});

		var mcpServer = McpServer.async(mcpServerTransportProvider)
			.serverInfo("test-server", "1.0.0")
			.tools(tool)
			.build();

		try (var mcpClient = clientBuilder.clientInfo(new Implementation("Sample client", "0.0.0"))
			.capabilities(ClientCapabilities.builder().sampling().build())
			.sampling(samplingHandler)
			.build()) {

			InitializeResult initResult = mcpClient.initialize();
			assertThat(initResult).isNotNull();

			CallToolResult response = mcpClient.callTool(new CallToolRequest("tool1", Map.of()));

			assertThat(response).isNotNull();
			assertThat(response).isEqualTo(callResponse);
		}
		mcpServer.close();
	}

	@Test
	void testCreateMessageWithRequestTimeoutSuccess() throws InterruptedException {

		// Client

		Function<CreateMessageRequest, CreateMessageResult> samplingHandler = request -> {
			assertThat(request.getMessages()).hasSize(1);
			assertThat(request.getMessages().get(0).getContent()).isInstanceOf(TextContent.class);
			try {
				TimeUnit.SECONDS.sleep(2);
			}
			catch (InterruptedException e) {
				throw new RuntimeException(e);
			}
			return new CreateMessageResult(Role.USER, new TextContent("Test message"), "MockModelName",
					CreateMessageResult.StopReason.STOP_SEQUENCE);
		};

		var mcpClient = clientBuilder.clientInfo(new Implementation("Sample client", "0.0.0"))
			.capabilities(ClientCapabilities.builder().sampling().build())
			.sampling(samplingHandler)
			.build();

		// Server

		CallToolResult callResponse = new CallToolResult(List.of(new TextContent("CALL RESPONSE")), false);

		McpServerFeatures.AsyncToolSpecification tool = new McpServerFeatures.AsyncToolSpecification(
				new Tool("tool1", "tool1 description", emptyJsonSchema), (exchange, request) -> {

					var craeteMessageRequest = CreateMessageRequest.builder()
						.messages(List.of(new SamplingMessage(Role.USER, new TextContent("Test message"))))
						.modelPreferences(ModelPreferences.builder().build())
						.build();

					StepVerifier.create(exchange.createMessage(craeteMessageRequest)).consumeNextWith(result -> {
						assertThat(result).isNotNull();
						assertThat(result.getRole()).isEqualTo(Role.USER);
						assertThat(result.getContent()).isInstanceOf(TextContent.class);
						assertThat(((TextContent) result.getContent()).getText()).isEqualTo("Test message");
						assertThat(result.getModel()).isEqualTo("MockModelName");
						assertThat(result.getStopReason()).isEqualTo(CreateMessageResult.StopReason.STOP_SEQUENCE);
					}).verifyComplete();

					return Mono.just(callResponse);
				});

		var mcpServer = McpServer.async(mcpServerTransportProvider)
			.serverInfo("test-server", "1.0.0")
			.requestTimeout(Duration.ofSeconds(3))
			.tools(tool)
			.build();

		InitializeResult initResult = mcpClient.initialize();
		assertThat(initResult).isNotNull();

		CallToolResult response = mcpClient.callTool(new CallToolRequest("tool1", Map.of()));

		assertThat(response).isNotNull();
		assertThat(response).isEqualTo(callResponse);

		mcpClient.close();
		mcpServer.close();
	}

	@Test
	void testCreateMessageWithRequestTimeoutFail() throws InterruptedException {

		// Client

		Function<CreateMessageRequest, CreateMessageResult> samplingHandler = request -> {
			assertThat(request.getMessages()).hasSize(1);
			assertThat(request.getMessages().get(0).getContent()).isInstanceOf(TextContent.class);
			try {
				TimeUnit.SECONDS.sleep(2);
			}
			catch (InterruptedException e) {
				throw new RuntimeException(e);
			}
			return new CreateMessageResult(Role.USER, new TextContent("Test message"), "MockModelName",
					CreateMessageResult.StopReason.STOP_SEQUENCE);
		};

		var mcpClient = clientBuilder.clientInfo(new Implementation("Sample client", "0.0.0"))
			.capabilities(ClientCapabilities.builder().sampling().build())
			.sampling(samplingHandler)
			.build();

		// Server

		CallToolResult callResponse = new CallToolResult(List.of(new TextContent("CALL RESPONSE")), false);

		McpServerFeatures.AsyncToolSpecification tool = new McpServerFeatures.AsyncToolSpecification(
				new Tool("tool1", "tool1 description", emptyJsonSchema), (exchange, request) -> {

					var craeteMessageRequest = CreateMessageRequest.builder()
						.messages(List.of(new SamplingMessage(Role.USER, new TextContent("Test message"))))
						.modelPreferences(ModelPreferences.builder().build())
						.build();

					StepVerifier.create(exchange.createMessage(craeteMessageRequest)).consumeNextWith(result -> {
						assertThat(result).isNotNull();
						assertThat(result.getRole()).isEqualTo(Role.USER);
						assertThat(result.getContent()).isInstanceOf(TextContent.class);
						assertThat(((TextContent) result.getContent()).getText()).isEqualTo("Test message");
						assertThat(result.getModel()).isEqualTo("MockModelName");
						assertThat(result.getStopReason()).isEqualTo(CreateMessageResult.StopReason.STOP_SEQUENCE);
					}).verifyComplete();

					return Mono.just(callResponse);
				});

		var mcpServer = McpServer.async(mcpServerTransportProvider)
			.serverInfo("test-server", "1.0.0")
			.requestTimeout(Duration.ofSeconds(1))
			.tools(tool)
			.build();

		InitializeResult initResult = mcpClient.initialize();
		assertThat(initResult).isNotNull();

		assertThatExceptionOfType(McpError.class).isThrownBy(() -> {
			mcpClient.callTool(new CallToolRequest("tool1", Map.of()));
		}).withMessageContaining("Timeout");

		mcpClient.close();
		mcpServer.close();
	}

	// ---------------------------------------
	// Roots Tests
	// ---------------------------------------
	@Test
	void testRootsSuccess() {
		List<Root> roots = List.of(new Root("uri1://", "root1"), new Root("uri2://", "root2"));

		AtomicReference<List<Root>> rootsRef = new AtomicReference<>();

		var mcpServer = McpServer.sync(mcpServerTransportProvider)
			.rootsChangeHandler((exchange, rootsUpdate) -> rootsRef.set(rootsUpdate))
			.build();

		try (var mcpClient = clientBuilder.capabilities(ClientCapabilities.builder().roots(true).build())
			.roots(roots)
			.build()) {

			InitializeResult initResult = mcpClient.initialize();
			assertThat(initResult).isNotNull();

			assertThat(rootsRef.get()).isNull();

			mcpClient.rootsListChangedNotification();

			await().atMost(Duration.ofSeconds(5)).untilAsserted(() -> {
				assertThat(rootsRef.get()).containsAll(roots);
			});

			// Remove a root
			mcpClient.removeRoot(roots.get(0).getUri());

			await().atMost(Duration.ofSeconds(5)).untilAsserted(() -> {
				assertThat(rootsRef.get()).containsAll(List.of(roots.get(1)));
			});

			// Add a new root
			var root3 = new Root("uri3://", "root3");
			mcpClient.addRoot(root3);

			await().atMost(Duration.ofSeconds(5)).untilAsserted(() -> {
				assertThat(rootsRef.get()).containsAll(List.of(roots.get(1), root3));
			});

			mcpServer.close();
		}
	}

	@Test
	void testRootsWithoutCapability() {

		McpServerFeatures.SyncToolSpecification tool = new McpServerFeatures.SyncToolSpecification(
				new Tool("tool1", "tool1 description", emptyJsonSchema), (exchange, request) -> {

					exchange.listRoots(); // try to list roots

					return mock(CallToolResult.class);
				});

		var mcpServer = McpServer.sync(mcpServerTransportProvider).rootsChangeHandler((exchange, rootsUpdate) -> {
		}).tools(tool).build();

		try (var mcpClient = clientBuilder.capabilities(ClientCapabilities.builder().build()).build()) {

			assertThat(mcpClient.initialize()).isNotNull();

			// Attempt to list roots should fail
			try {
				mcpClient.callTool(new CallToolRequest("tool1", Map.of()));
			}
			catch (McpError e) {
				assertThat(e).isInstanceOf(McpError.class).hasMessage("Roots not supported");
			}
		}

		mcpServer.close();
	}

	@Test
	void testRootsNotificationWithEmptyRootsList() {
		AtomicReference<List<Root>> rootsRef = new AtomicReference<>();

		var mcpServer = McpServer.sync(mcpServerTransportProvider)
			.rootsChangeHandler((exchange, rootsUpdate) -> rootsRef.set(rootsUpdate))
			.build();

		try (var mcpClient = clientBuilder.capabilities(ClientCapabilities.builder().roots(true).build())
			.roots(List.of()) // Empty roots list
			.build()) {

			InitializeResult initResult = mcpClient.initialize();
			assertThat(initResult).isNotNull();

			mcpClient.rootsListChangedNotification();

			await().atMost(Duration.ofSeconds(5)).untilAsserted(() -> {
				assertThat(rootsRef.get()).isEmpty();
			});
		}

		mcpServer.close();
	}

	@Test
	void testRootsWithMultipleHandlers() {
		List<Root> roots = List.of(new Root("uri1://", "root1"));

		AtomicReference<List<Root>> rootsRef1 = new AtomicReference<>();
		AtomicReference<List<Root>> rootsRef2 = new AtomicReference<>();

		var mcpServer = McpServer.sync(mcpServerTransportProvider)
			.rootsChangeHandler((exchange, rootsUpdate) -> rootsRef1.set(rootsUpdate))
			.rootsChangeHandler((exchange, rootsUpdate) -> rootsRef2.set(rootsUpdate))
			.build();

		try (var mcpClient = clientBuilder.capabilities(ClientCapabilities.builder().roots(true).build())
			.roots(roots)
			.build()) {

			assertThat(mcpClient.initialize()).isNotNull();

			mcpClient.rootsListChangedNotification();

			await().atMost(Duration.ofSeconds(5)).untilAsserted(() -> {
				assertThat(rootsRef1.get()).containsAll(roots);
				assertThat(rootsRef2.get()).containsAll(roots);
			});
		}

		mcpServer.close();
	}

	@Test
	void testRootsServerCloseWithActiveSubscription() {
		List<Root> roots = List.of(new Root("uri1://", "root1"));

		AtomicReference<List<Root>> rootsRef = new AtomicReference<>();

		var mcpServer = McpServer.sync(mcpServerTransportProvider)
			.rootsChangeHandler((exchange, rootsUpdate) -> rootsRef.set(rootsUpdate))
			.build();

		try (var mcpClient = clientBuilder.capabilities(ClientCapabilities.builder().roots(true).build())
			.roots(roots)
			.build()) {

			InitializeResult initResult = mcpClient.initialize();
			assertThat(initResult).isNotNull();

			mcpClient.rootsListChangedNotification();

			await().atMost(Duration.ofSeconds(5)).untilAsserted(() -> {
				assertThat(rootsRef.get()).containsAll(roots);
			});
		}

		mcpServer.close();
	}

	// ---------------------------------------
	// Tools Tests
	// ---------------------------------------

	String emptyJsonSchema = "{\n" + "	\"$schema\": \"http://json-schema.org/draft-07/schema#\",\n"
			+ "	\"type\": \"object\",\n" + "	\"properties\": {}\n" + "}";

	@Test
	void testToolCallSuccess() {

		var callResponse = new CallToolResult(List.of(new TextContent("CALL RESPONSE")), false);
		McpServerFeatures.SyncToolSpecification tool1 = new McpServerFeatures.SyncToolSpecification(
				new Tool("tool1", "tool1 description", emptyJsonSchema), (exchange, request) -> {
					// perform a blocking call to a remote service
					String response = restTemplate.getForObject(
							"https://raw.githubusercontent.com/modelcontextprotocol/java-sdk/refs/heads/main/README.md",
							String.class);
					assertThat(response).isNotBlank();
					return callResponse;
				});

		var mcpServer = McpServer.sync(mcpServerTransportProvider)
			.capabilities(ServerCapabilities.builder().tools(true).build())
			.tools(tool1)
			.build();

		try (var mcpClient = clientBuilder.build()) {
			InitializeResult initResult = mcpClient.initialize();
			assertThat(initResult).isNotNull();

			assertThat(mcpClient.listTools().getTools()).contains(tool1.getTool());

			CallToolResult response = mcpClient.callTool(new CallToolRequest("tool1", Map.of()));

			assertThat(response).isNotNull();
			assertThat(response).isEqualTo(callResponse);
		}

		mcpServer.close();
	}

	@Test
	void testToolListChangeHandlingSuccess() {

		var callResponse = new CallToolResult(List.of(new TextContent("CALL RESPONSE")), false);
		McpServerFeatures.SyncToolSpecification tool1 = new McpServerFeatures.SyncToolSpecification(
				new Tool("tool1", "tool1 description", emptyJsonSchema), (exchange, request) -> {
					// perform a blocking call to a remote service
					String response = restTemplate.getForObject(
							"https://raw.githubusercontent.com/modelcontextprotocol/java-sdk/refs/heads/main/README.md",
							String.class);
					assertThat(response).isNotBlank();
					return callResponse;
				});

		AtomicReference<List<Tool>> rootsRef = new AtomicReference<>();

		var mcpServer = McpServer.sync(mcpServerTransportProvider)
			.capabilities(ServerCapabilities.builder().tools(true).build())
			.tools(tool1)
			.build();

		try (var mcpClient = clientBuilder.toolsChangeConsumer(toolsUpdate -> {
			// perform a blocking call to a remote service
			String response = restTemplate.getForObject(
					"https://raw.githubusercontent.com/modelcontextprotocol/java-sdk/refs/heads/main/README.md",
					String.class);
			assertThat(response).isNotBlank();
			rootsRef.set(toolsUpdate);
		}).build()) {

			InitializeResult initResult = mcpClient.initialize();
			assertThat(initResult).isNotNull();

			assertThat(rootsRef.get()).isNull();

			assertThat(mcpClient.listTools().getTools()).contains(tool1.getTool());

			mcpServer.notifyToolsListChanged();

			await().atMost(Duration.ofSeconds(5)).untilAsserted(() -> {
				assertThat(rootsRef.get()).containsAll(List.of(tool1.getTool()));
			});

			// Remove a tool
			mcpServer.removeTool("tool1");

			await().atMost(Duration.ofSeconds(5)).untilAsserted(() -> {
				assertThat(rootsRef.get()).isEmpty();
			});

			// Add a new tool
			McpServerFeatures.SyncToolSpecification tool2 = new McpServerFeatures.SyncToolSpecification(
					new Tool("tool2", "tool2 description", emptyJsonSchema), (exchange, request) -> callResponse);

			mcpServer.addTool(tool2);

			await().atMost(Duration.ofSeconds(5)).untilAsserted(() -> {
				assertThat(rootsRef.get()).containsAll(List.of(tool2.getTool()));
			});
		}

		mcpServer.close();
	}

	@Test
	void testInitialize() {
		var mcpServer = McpServer.sync(mcpServerTransportProvider).build();

		try (var mcpClient = clientBuilder.build()) {

			InitializeResult initResult = mcpClient.initialize();
			assertThat(initResult).isNotNull();
		}

		mcpServer.close();
	}

	// ---------------------------------------
	// Logging Tests
	// ---------------------------------------
	@Test
	void testLoggingNotification() {
		// Create a list to store received logging notifications
		List<LoggingMessageNotification> receivedNotifications = new ArrayList<>();

		// Create server with a tool that sends logging notifications
		McpServerFeatures.AsyncToolSpecification tool = new McpServerFeatures.AsyncToolSpecification(
				new Tool("logging-test", "Test logging notifications", emptyJsonSchema), (exchange, request) -> {

					// Create and send notifications with different levels

					// This should be filtered out (DEBUG < NOTICE)
					exchange
						.loggingNotification(LoggingMessageNotification.builder()
							.level(LoggingLevel.DEBUG)
							.logger("test-logger")
							.data("Debug message")
							.build())
						.block();

					// This should be sent (NOTICE >= NOTICE)
					exchange
						.loggingNotification(LoggingMessageNotification.builder()
							.level(LoggingLevel.NOTICE)
							.logger("test-logger")
							.data("Notice message")
							.build())
						.block();

					// This should be sent (ERROR > NOTICE)
					exchange
						.loggingNotification(LoggingMessageNotification.builder()
							.level(LoggingLevel.ERROR)
							.logger("test-logger")
							.data("Error message")
							.build())
						.block();

					// This should be filtered out (INFO < NOTICE)
					exchange
						.loggingNotification(LoggingMessageNotification.builder()
							.level(LoggingLevel.INFO)
							.logger("test-logger")
							.data("Another info message")
							.build())
						.block();

					// This should be sent (ERROR >= NOTICE)
					exchange
						.loggingNotification(LoggingMessageNotification.builder()
							.level(LoggingLevel.ERROR)
							.logger("test-logger")
							.data("Another error message")
							.build())
						.block();

					return Mono.just(new CallToolResult("Logging test completed", false));
				});

		var mcpServer = McpServer.async(mcpServerTransportProvider)
			.serverInfo("test-server", "1.0.0")
			.capabilities(ServerCapabilities.builder().logging().tools(true).build())
			.tools(tool)
			.build();
		try (
				// Create client with logging notification handler
				var mcpClient = clientBuilder.loggingConsumer(notification -> {
					receivedNotifications.add(notification);
				}).build()) {

			// Initialize client
			InitializeResult initResult = mcpClient.initialize();
			assertThat(initResult).isNotNull();

			// Set minimum logging level to NOTICE
			mcpClient.setLoggingLevel(LoggingLevel.NOTICE);

			// Call the tool that sends logging notifications
			CallToolResult result = mcpClient.callTool(new CallToolRequest("logging-test", Map.of()));
			assertThat(result).isNotNull();
			assertThat(result.getContent().get(0)).isInstanceOf(TextContent.class);
			assertThat(((TextContent) result.getContent().get(0)).getText()).isEqualTo("Logging test completed");

			// Wait for notifications to be processed
			await().atMost(Duration.ofSeconds(5)).untilAsserted(() -> {

				System.out.println("Received notifications: " + receivedNotifications);

				// Should have received 3 notifications (1 NOTICE and 2 ERROR)
				assertThat(receivedNotifications).hasSize(3);

				Map<String, LoggingMessageNotification> notificationMap = receivedNotifications.stream()
					.collect(Collectors.toMap(n -> n.getData(), n -> n));

				// First notification should be NOTICE level
				assertThat(notificationMap.get("Notice message").getLevel()).isEqualTo(LoggingLevel.NOTICE);
				assertThat(notificationMap.get("Notice message").getLogger()).isEqualTo("test-logger");
				assertThat(notificationMap.get("Notice message").getData()).isEqualTo("Notice message");

				// Second notification should be ERROR level
				assertThat(notificationMap.get("Error message").getLevel()).isEqualTo(LoggingLevel.ERROR);
				assertThat(notificationMap.get("Error message").getLogger()).isEqualTo("test-logger");
				assertThat(notificationMap.get("Error message").getData()).isEqualTo("Error message");

				// Third notification should be ERROR level
				assertThat(notificationMap.get("Another error message").getLevel()).isEqualTo(LoggingLevel.ERROR);
				assertThat(notificationMap.get("Another error message").getLogger()).isEqualTo("test-logger");
				assertThat(notificationMap.get("Another error message").getData()).isEqualTo("Another error message");
			});
		}
		mcpServer.close();
	}

	@Test
	void testSendingRequestToNonExistentEndpoint() {
		RequestEntity<JSONRPCRequest> request = RequestEntity
			.post(URI.create(String.format("http://localhost:%d/bad-endpoint", PORT)))
			.contentType(MediaType.APPLICATION_JSON)
			.body(new JSONRPCRequest("2.0", "test", 1, "Hello, World!"));
		ResponseEntity<String> response = restTemplate.exchange(request, String.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
	}

	@Test
	void testSendingRequestWithInvalidContentType() {
		RequestEntity<JSONRPCRequest> request = RequestEntity
			.post(URI.create(String.format("http://localhost:%d%s", PORT, CUSTOM_MESSAGE_ENDPOINT)))
			.contentType(MediaType.TEXT_PLAIN)
			.body(new JSONRPCRequest("2.0", "test", 1, "Hello, World!"));
		ResponseEntity<String> response = restTemplate.exchange(request, String.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNSUPPORTED_MEDIA_TYPE);
	}

	@Test
	void testSendingInvalidJsonRpcMessage() {
		RequestEntity<String> request = RequestEntity
			.post(URI.create(String.format("http://localhost:%d%s", PORT, CUSTOM_MESSAGE_ENDPOINT)))
			.contentType(MediaType.APPLICATION_JSON)
			.body("invalid json");
		ResponseEntity<String> response = restTemplate.exchange(request, String.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
	}

	@Test
	void testHandleMessageRequest() throws Exception {
		ResponseEntity<String> response = sendRequest("test", "Hello, World!");
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(response.getHeaders().getContentType()).isEqualTo(MediaType.APPLICATION_JSON);

		JSONRPCResponse responseMessage = objectMapper.readValue(response.getBody(), JSONRPCResponse.class);
		assertThat(responseMessage.getResult()).isEqualTo("Hello, World!");
	}

	@Test
	void testHandleMessageRequestWithError() throws Exception {
		ResponseEntity<String> response = sendRequest("error", null);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
		assertThat(response.getHeaders().getContentType()).isEqualTo(MediaType.APPLICATION_JSON);

		JSONRPCResponse responseMessage = objectMapper.readValue(response.getBody(), JSONRPCResponse.class);
		assertThat(responseMessage.getError()).isNotNull();
		assertThat(responseMessage.getError().getMessage()).isEqualTo("Test error");
	}

	@Test
	void testHandleMessageRequestWithInvalidMethod() throws Exception {
		ResponseEntity<String> response = sendRequest("invalid", null);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
		assertThat(response.getHeaders().getContentType()).isEqualTo(MediaType.APPLICATION_JSON);

		JSONRPCResponse responseMessage = objectMapper.readValue(response.getBody(), JSONRPCResponse.class);
		assertThat(responseMessage.getError()).isNotNull();
		assertThat(responseMessage.getError().getMessage()).isEqualTo("Method not found: invalid");
	}

	private ResponseEntity<String> sendRequest(String method, Object params) {
		RequestEntity<JSONRPCRequest> request = RequestEntity
			.post(URI.create(String.format("http://localhost:%d%s", PORT, CUSTOM_MESSAGE_ENDPOINT)))
			.contentType(MediaType.APPLICATION_JSON)
			.body(new JSONRPCRequest("2.0", method, 1, params));
		return restTemplate.exchange(request, String.class);
	}

}