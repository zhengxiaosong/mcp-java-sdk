/*
 * Copyright 2024-2024 the original author or authors.
 */

package io.modelcontextprotocol.client;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.modelcontextprotocol.MockMcpClientTransport;
import io.modelcontextprotocol.spec.common.ListRootsResult;
import io.modelcontextprotocol.spec.common.Root;
import io.modelcontextprotocol.spec.common.Role;
import io.modelcontextprotocol.spec.content.TextContent;
import io.modelcontextprotocol.spec.initialization.ClientCapabilities;
import io.modelcontextprotocol.spec.initialization.Implementation;
import io.modelcontextprotocol.spec.initialization.InitializeResult;
import io.modelcontextprotocol.spec.initialization.ServerCapabilities;
import io.modelcontextprotocol.spec.jsonrpc.ErrorCodes;
import io.modelcontextprotocol.spec.jsonrpc.JSONRPCNotification;
import io.modelcontextprotocol.spec.jsonrpc.JSONRPCRequest;
import io.modelcontextprotocol.spec.jsonrpc.JSONRPCResponse;
import io.modelcontextprotocol.spec.prompt.ListPromptsResult;
import io.modelcontextprotocol.spec.prompt.Prompt;
import io.modelcontextprotocol.spec.prompt.PromptArgument;
import io.modelcontextprotocol.spec.resource.ListResourcesResult;
import io.modelcontextprotocol.spec.resource.Resource;
import io.modelcontextprotocol.spec.sampling.CreateMessageRequest;
import io.modelcontextprotocol.spec.sampling.CreateMessageResult;
import io.modelcontextprotocol.spec.tool.ListToolsResult;
import io.modelcontextprotocol.spec.tool.Tool;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

import static io.modelcontextprotocol.spec.McpSchema.JSONRPC_VERSION;
import static io.modelcontextprotocol.spec.McpSchema.LATEST_PROTOCOL_VERSION;
import static io.modelcontextprotocol.spec.McpSchema.METHOD_INITIALIZE;
import static io.modelcontextprotocol.spec.McpSchema.METHOD_NOTIFICATION_INITIALIZED;
import static io.modelcontextprotocol.spec.McpSchema.METHOD_NOTIFICATION_PROMPTS_LIST_CHANGED;
import static io.modelcontextprotocol.spec.McpSchema.METHOD_NOTIFICATION_RESOURCES_LIST_CHANGED;
import static io.modelcontextprotocol.spec.McpSchema.METHOD_NOTIFICATION_TOOLS_LIST_CHANGED;
import static io.modelcontextprotocol.spec.McpSchema.METHOD_PROMPT_LIST;
import static io.modelcontextprotocol.spec.McpSchema.METHOD_RESOURCES_LIST;
import static io.modelcontextprotocol.spec.McpSchema.METHOD_ROOTS_LIST;
import static io.modelcontextprotocol.spec.McpSchema.METHOD_TOOLS_LIST;
import static io.modelcontextprotocol.spec.McpSchema.METHOD_SAMPLING_CREATE_MESSAGE;
import static org.assertj.core.api.Assertions.assertThat;

class McpAsyncClientResponseHandlerTests {

	private static final Implementation SERVER_INFO = new Implementation("test-server", "1.0.0");

	private static final ServerCapabilities SERVER_CAPABILITIES = ServerCapabilities.builder()
		.tools(true)
		.resources(true, true) // Enable both resources and resource templates
		.build();

	private static MockMcpClientTransport initializationEnabledTransport() {
		return initializationEnabledTransport(SERVER_CAPABILITIES, SERVER_INFO);
	}

	private static MockMcpClientTransport initializationEnabledTransport(ServerCapabilities mockServerCapabilities,
			Implementation mockServerInfo) {
		InitializeResult mockInitResult = new InitializeResult(LATEST_PROTOCOL_VERSION, mockServerCapabilities,
				mockServerInfo, "Test instructions");

		return new MockMcpClientTransport((t, message) -> {
			if (message instanceof JSONRPCRequest r && METHOD_INITIALIZE.equals(r.getMethod())) {
				JSONRPCResponse initResponse = new JSONRPCResponse(JSONRPC_VERSION, r.getId(), mockInitResult, null);
				t.simulateIncomingMessage(initResponse);
			}
		});
	}

	@Test
	void testSuccessfulInitialization() {
		Implementation serverInfo = new Implementation("mcp-test-server", "0.0.1");
		ServerCapabilities serverCapabilities = ServerCapabilities.builder()
			.tools(false)
			.resources(true, true) // Enable both resources and resource templates
			.build();
		MockMcpClientTransport transport = initializationEnabledTransport(serverCapabilities, serverInfo);
		McpAsyncClient asyncMcpClient = McpClient.async(transport).build();

		// Verify client is not initialized initially
		assertThat(asyncMcpClient.isInitialized()).isFalse();

		// Start initialization with reactive handling
		InitializeResult result = asyncMcpClient.initialize().block();

		// Verify initialized notification was sent
		JSONRPCNotification notification = transport.getLastSentMessageAsNotification();
		assertThat(notification.getMethod()).isEqualTo(METHOD_NOTIFICATION_INITIALIZED);

		// Verify initialization result
		assertThat(result).isNotNull();
		assertThat(result.getProtocolVersion()).isEqualTo(LATEST_PROTOCOL_VERSION);
		assertThat(result.getCapabilities()).isEqualTo(serverCapabilities);
		assertThat(result.getServerInfo()).isEqualTo(serverInfo);
		assertThat(result.getInstructions()).isEqualTo("Test instructions");

		// Verify client state after initialization
		assertThat(asyncMcpClient.isInitialized()).isTrue();
		assertThat(asyncMcpClient.getServerCapabilities()).isEqualTo(serverCapabilities);
		assertThat(asyncMcpClient.getServerInfo()).isEqualTo(serverInfo);

		asyncMcpClient.closeGracefully();
	}

	@Test
	void testToolsChangeNotificationHandling() throws JsonProcessingException {
		MockMcpClientTransport transport = initializationEnabledTransport();

		// Create a list to store received tools for verification
		List<Tool> receivedTools = new ArrayList<>();

		// Create a consumer that will be called when tools change
		Function<List<Tool>, Mono<Void>> toolsChangeConsumer = tools -> Mono
			.fromRunnable(() -> receivedTools.addAll(tools));

		// Create client with tools change consumer
		McpAsyncClient asyncMcpClient = McpClient.async(transport).toolsChangeConsumer(toolsChangeConsumer).build();

		assertThat(asyncMcpClient.initialize().block()).isNotNull();

		// Create a mock tools list that the server will return
		Map<String, Object> inputSchema = Map.of("type", "object", "properties", Map.of(), "required", List.of());
		Tool mockTool = new Tool("test-tool", "Test Tool Description",
				new ObjectMapper().writeValueAsString(inputSchema));
		ListToolsResult mockToolsResult = new ListToolsResult(List.of(mockTool), null);

		// Simulate server sending tools/list_changed notification
		JSONRPCNotification notification = new JSONRPCNotification(JSONRPC_VERSION,
				METHOD_NOTIFICATION_TOOLS_LIST_CHANGED, null);
		transport.simulateIncomingMessage(notification);

		// Simulate server response to tools/list request
		JSONRPCRequest toolsListRequest = transport.getLastSentMessageAsRequest();
		assertThat(toolsListRequest.getMethod()).isEqualTo(METHOD_TOOLS_LIST);

		JSONRPCResponse toolsListResponse = new JSONRPCResponse(JSONRPC_VERSION, toolsListRequest.getId(),
				mockToolsResult, null);
		transport.simulateIncomingMessage(toolsListResponse);

		// Verify the consumer received the expected tools
		assertThat(receivedTools).hasSize(1);
		assertThat(receivedTools.get(0).getName()).isEqualTo("test-tool");
		assertThat(receivedTools.get(0).getDescription()).isEqualTo("Test Tool Description");

		asyncMcpClient.closeGracefully();
	}

	@Test
	void testRootsListRequestHandling() {
		MockMcpClientTransport transport = initializationEnabledTransport();

		McpAsyncClient asyncMcpClient = McpClient.async(transport)
			.roots(new Root("file:///test/path", "test-root"))
			.build();

		assertThat(asyncMcpClient.initialize().block()).isNotNull();

		// Simulate incoming request
		JSONRPCRequest request = new JSONRPCRequest(JSONRPC_VERSION, METHOD_ROOTS_LIST, "test-id", null);
		transport.simulateIncomingMessage(request);

		// Verify response
		assertThat(transport.getLastSentMessage()).isInstanceOf(JSONRPCResponse.class);

		JSONRPCResponse response = (JSONRPCResponse) transport.getLastSentMessage();
		assertThat(response.getId()).isEqualTo("test-id");
		assertThat(response.getResult())
			.isEqualTo(new ListRootsResult(List.of(new Root("file:///test/path", "test-root"))));
		assertThat(response.getError()).isNull();

		asyncMcpClient.closeGracefully();
	}

	@Test
	void testResourcesChangeNotificationHandling() {
		MockMcpClientTransport transport = initializationEnabledTransport();

		// Create a list to store received resources for verification
		List<Resource> receivedResources = new ArrayList<>();

		// Create a consumer that will be called when resources change
		Function<List<Resource>, Mono<Void>> resourcesChangeConsumer = resources -> Mono
			.fromRunnable(() -> receivedResources.addAll(resources));

		// Create client with resources change consumer
		McpAsyncClient asyncMcpClient = McpClient.async(transport)
			.resourcesChangeConsumer(resourcesChangeConsumer)
			.build();

		assertThat(asyncMcpClient.initialize().block()).isNotNull();

		// Create a mock resources list that the server will return
		Resource mockResource = new Resource("test://resource", "Test Resource", "A test resource", "text/plain", null);
		ListResourcesResult mockResourcesResult = new ListResourcesResult(List.of(mockResource), null);

		// Simulate server sending resources/list_changed notification
		JSONRPCNotification notification = new JSONRPCNotification(JSONRPC_VERSION,
				METHOD_NOTIFICATION_RESOURCES_LIST_CHANGED, null);
		transport.simulateIncomingMessage(notification);

		// Simulate server response to resources/list request
		JSONRPCRequest resourcesListRequest = transport.getLastSentMessageAsRequest();
		assertThat(resourcesListRequest.getMethod()).isEqualTo(METHOD_RESOURCES_LIST);

		JSONRPCResponse resourcesListResponse = new JSONRPCResponse(JSONRPC_VERSION, resourcesListRequest.getId(),
				mockResourcesResult, null);
		transport.simulateIncomingMessage(resourcesListResponse);

		// Verify the consumer received the expected resources
		assertThat(receivedResources).hasSize(1);
		assertThat(receivedResources.get(0).getUri()).isEqualTo("test://resource");
		assertThat(receivedResources.get(0).getName()).isEqualTo("Test Resource");
		assertThat(receivedResources.get(0).getDescription()).isEqualTo("A test resource");

		asyncMcpClient.closeGracefully();
	}

	@Test
	void testPromptsChangeNotificationHandling() {
		MockMcpClientTransport transport = initializationEnabledTransport();

		// Create a list to store received prompts for verification
		List<Prompt> receivedPrompts = new ArrayList<>();

		// Create a consumer that will be called when prompts change
		Function<List<Prompt>, Mono<Void>> promptsChangeConsumer = prompts -> Mono
			.fromRunnable(() -> receivedPrompts.addAll(prompts));

		// Create client with prompts change consumer
		McpAsyncClient asyncMcpClient = McpClient.async(transport).promptsChangeConsumer(promptsChangeConsumer).build();

		assertThat(asyncMcpClient.initialize().block()).isNotNull();

		// Create a mock prompts list that the server will return
		Prompt mockPrompt = new Prompt("test-prompt", "Test Prompt Description",
				List.of(new PromptArgument("arg1", "Test argument", true)));
		ListPromptsResult mockPromptsResult = new ListPromptsResult(List.of(mockPrompt), null);

		// Simulate server sending prompts/list_changed notification
		JSONRPCNotification notification = new JSONRPCNotification(JSONRPC_VERSION,
				METHOD_NOTIFICATION_PROMPTS_LIST_CHANGED, null);
		transport.simulateIncomingMessage(notification);

		// Simulate server response to prompts/list request
		JSONRPCRequest promptsListRequest = transport.getLastSentMessageAsRequest();
		assertThat(promptsListRequest.getMethod()).isEqualTo(METHOD_PROMPT_LIST);

		JSONRPCResponse promptsListResponse = new JSONRPCResponse(JSONRPC_VERSION, promptsListRequest.getId(),
				mockPromptsResult, null);
		transport.simulateIncomingMessage(promptsListResponse);

		// Verify the consumer received the expected prompts
		assertThat(receivedPrompts).hasSize(1);
		assertThat(receivedPrompts.get(0).getName()).isEqualTo("test-prompt");
		assertThat(receivedPrompts.get(0).getDescription()).isEqualTo("Test Prompt Description");
		assertThat(receivedPrompts.get(0).getArguments()).hasSize(1);
		assertThat(receivedPrompts.get(0).getArguments().get(0).getName()).isEqualTo("arg1");

		asyncMcpClient.closeGracefully();
	}

	@Test
	void testSamplingCreateMessageRequestHandling() {
		MockMcpClientTransport transport = initializationEnabledTransport();
		ClientCapabilities clientCapabilities = ClientCapabilities.builder().sampling().build();

		McpAsyncClient asyncMcpClient = McpClient.async(transport)
			.clientCapabilities(clientCapabilities)
			.sampling((CreateMessageRequest request) -> {
				return Mono.just(new CreateMessageResult(Role.ASSISTANT, new TextContent("Test response"), "test-model",
						CreateMessageResult.StopReason.MAX_TOKENS));
			})
			.build();

		assertThat(asyncMcpClient.initialize().block()).isNotNull();

		// Simulate incoming request
		JSONRPCRequest request = new JSONRPCRequest(JSONRPC_VERSION, METHOD_SAMPLING_CREATE_MESSAGE, "test-id", null);
		transport.simulateIncomingMessage(request);

		// Verify response
		assertThat(transport.getLastSentMessage()).isInstanceOf(JSONRPCResponse.class);

		JSONRPCResponse response = (JSONRPCResponse) transport.getLastSentMessage();
		assertThat(response.getId()).isEqualTo("test-id");
		assertThat(response.getResult()).isNotNull();
		assertThat(response.getResult()).isInstanceOf(CreateMessageResult.class);
		CreateMessageResult result = (CreateMessageResult) response.getResult();
		assertThat(result.getRole()).isEqualTo(Role.ASSISTANT);
		assertThat(result.getContent()).isEqualTo(new TextContent("Test response"));
		assertThat(result.getModel()).isEqualTo("test-model");
		assertThat(result.getStopReason()).isEqualTo(CreateMessageResult.StopReason.MAX_TOKENS);
		assertThat(response.getError()).isNull();

		asyncMcpClient.closeGracefully();
	}

	@Test
	void testSamplingCreateMessageRequestHandlingWithoutCapability() {
		MockMcpClientTransport transport = initializationEnabledTransport();
		// don't set sampling capability
		ClientCapabilities clientCapabilities = new ClientCapabilities(null, null, null);

		McpAsyncClient asyncMcpClient = McpClient.async(transport)
			.clientCapabilities(clientCapabilities)
			.sampling((CreateMessageRequest request) -> {
				return Mono.just(new CreateMessageResult(Role.ASSISTANT, new TextContent("Test response"), "test-model",
						CreateMessageResult.StopReason.MAX_TOKENS));
			})
			.build();

		assertThat(asyncMcpClient.initialize().block()).isNotNull();

		// Simulate incoming request
		JSONRPCRequest request = new JSONRPCRequest(JSONRPC_VERSION, METHOD_SAMPLING_CREATE_MESSAGE, "test-id", null);
		transport.simulateIncomingMessage(request);

		// Verify response
		assertThat(transport.getLastSentMessage()).isInstanceOf(JSONRPCResponse.class);

		JSONRPCResponse response = (JSONRPCResponse) transport.getLastSentMessage();
		assertThat(response.getId()).isEqualTo("test-id");
		assertThat(response.getResult()).isNull();
		assertThat(response.getError()).isNotNull();
		assertThat(response.getError().getCode()).isEqualTo(ErrorCodes.METHOD_NOT_FOUND);

		asyncMcpClient.closeGracefully();
	}

	@Test
	void testSamplingCreateMessageRequestHandlingWithNullHandler() {
		MockMcpClientTransport transport = initializationEnabledTransport();
		ClientCapabilities clientCapabilities = new ClientCapabilities(null, null, new ClientCapabilities.Sampling());

		McpAsyncClient asyncMcpClient = McpClient.async(transport)
			.clientCapabilities(clientCapabilities)
			// don't set createMessageRequestHandler
			.build();

		assertThat(asyncMcpClient.initialize().block()).isNotNull();

		// Simulate incoming request
		JSONRPCRequest request = new JSONRPCRequest(JSONRPC_VERSION, METHOD_SAMPLING_CREATE_MESSAGE, "test-id", null);
		transport.simulateIncomingMessage(request);

		// Verify response
		assertThat(transport.getLastSentMessage()).isInstanceOf(JSONRPCResponse.class);

		JSONRPCResponse response = (JSONRPCResponse) transport.getLastSentMessage();
		assertThat(response.getId()).isEqualTo("test-id");
		assertThat(response.getResult()).isNull();
		assertThat(response.getError()).isNotNull();
		assertThat(response.getError().getCode()).isEqualTo(ErrorCodes.METHOD_NOT_FOUND);

		asyncMcpClient.closeGracefully();
	}

}
