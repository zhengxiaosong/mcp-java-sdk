/*
* Copyright 2025 - 2025 the original author or authors.
*/
package io.modelcontextprotocol.spec;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.InvalidTypeIdException;
import io.modelcontextprotocol.spec.common.Annotations;
import io.modelcontextprotocol.spec.common.ListRootsResult;
import io.modelcontextprotocol.spec.common.Role;
import io.modelcontextprotocol.spec.common.Root;
import io.modelcontextprotocol.spec.content.Content;
import io.modelcontextprotocol.spec.content.EmbeddedResource;
import io.modelcontextprotocol.spec.content.ImageContent;
import io.modelcontextprotocol.spec.content.TextContent;
import io.modelcontextprotocol.spec.initialization.ClientCapabilities;
import io.modelcontextprotocol.spec.initialization.Implementation;
import io.modelcontextprotocol.spec.initialization.InitializeRequest;
import io.modelcontextprotocol.spec.initialization.InitializeResult;
import io.modelcontextprotocol.spec.initialization.ServerCapabilities;
import io.modelcontextprotocol.spec.jsonrpc.ErrorCodes;
import io.modelcontextprotocol.spec.jsonrpc.JSONRPCNotification;
import io.modelcontextprotocol.spec.jsonrpc.JSONRPCRequest;
import io.modelcontextprotocol.spec.jsonrpc.JSONRPCResponse;
import io.modelcontextprotocol.spec.prompt.GetPromptRequest;
import io.modelcontextprotocol.spec.prompt.GetPromptResult;
import io.modelcontextprotocol.spec.prompt.ListPromptsResult;
import io.modelcontextprotocol.spec.prompt.Prompt;
import io.modelcontextprotocol.spec.prompt.PromptArgument;
import io.modelcontextprotocol.spec.prompt.PromptMessage;
import io.modelcontextprotocol.spec.resource.BlobResourceContents;
import io.modelcontextprotocol.spec.resource.ListResourceTemplatesResult;
import io.modelcontextprotocol.spec.resource.ListResourcesResult;
import io.modelcontextprotocol.spec.resource.ReadResourceRequest;
import io.modelcontextprotocol.spec.resource.ReadResourceResult;
import io.modelcontextprotocol.spec.resource.Resource;
import io.modelcontextprotocol.spec.resource.ResourceTemplate;
import io.modelcontextprotocol.spec.resource.TextResourceContents;
import io.modelcontextprotocol.spec.sampling.CreateMessageRequest;
import io.modelcontextprotocol.spec.sampling.CreateMessageResult;
import io.modelcontextprotocol.spec.sampling.ModelHint;
import io.modelcontextprotocol.spec.sampling.ModelPreferences;
import io.modelcontextprotocol.spec.sampling.SamplingMessage;
import io.modelcontextprotocol.spec.tool.CallToolRequest;
import io.modelcontextprotocol.spec.tool.CallToolResult;
import io.modelcontextprotocol.spec.tool.JsonSchema;
import io.modelcontextprotocol.spec.tool.Tool;
import net.javacrumbs.jsonunit.core.Option;
import org.junit.jupiter.api.Test;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static net.javacrumbs.jsonunit.assertj.JsonAssertions.json;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * @author Christian Tzolov
 */
public class McpSchemaTests {

	ObjectMapper mapper = new ObjectMapper();

	// Content Types Tests

	@Test
	void testTextContent() throws Exception {
		TextContent test = new TextContent("XXX");
		String value = mapper.writeValueAsString(test);

		assertThatJson(value).when(Option.IGNORING_ARRAY_ORDER)
			.when(Option.IGNORING_EXTRA_ARRAY_ITEMS)
			.isObject()
			.isEqualTo(json("""
					{"type":"text","text":"XXX"}"""));
	}

	@Test
	void testTextContentDeserialization() throws Exception {
		TextContent textContent = mapper.readValue("""
				{"type":"text","text":"XXX"}""", TextContent.class);

		assertThat(textContent).isNotNull();
		assertThat(textContent.getType()).isEqualTo("text");
		assertThat(textContent.getText()).isEqualTo("XXX");
	}

	@Test
	void testContentDeserializationWrongType() throws Exception {

		assertThatThrownBy(() -> mapper.readValue("""
				{"type":"WRONG","text":"XXX"}""", Content.class)).isInstanceOf(InvalidTypeIdException.class)
			.hasMessageContaining(
					"Could not resolve type id 'WRONG' as a subtype of `io.modelcontextprotocol.spec.content.Content`: known type ids = [image, resource, text]");
	}

	@Test
	void testImageContent() throws Exception {
		ImageContent test = new ImageContent(null, null, "base64encodeddata", "image/png");
		String value = mapper.writeValueAsString(test);

		assertThatJson(value).when(Option.IGNORING_ARRAY_ORDER)
			.when(Option.IGNORING_EXTRA_ARRAY_ITEMS)
			.isObject()
			.isEqualTo(json("""
					{"type":"image","data":"base64encodeddata","mimeType":"image/png"}"""));
	}

	@Test
	void testImageContentDeserialization() throws Exception {
		ImageContent imageContent = mapper.readValue("""
				{"type":"image","data":"base64encodeddata","mimeType":"image/png"}""", ImageContent.class);
		assertThat(imageContent).isNotNull();
		assertThat(imageContent.getType()).isEqualTo("image");
		assertThat(imageContent.getData()).isEqualTo("base64encodeddata");
		assertThat(imageContent.getMimeType()).isEqualTo("image/png");
	}

	@Test
	void testEmbeddedResource() throws Exception {
		TextResourceContents resourceContents = new TextResourceContents("resource://test", "text/plain",
				"Sample resource content");

		EmbeddedResource test = new EmbeddedResource(null, null, resourceContents);

		String value = mapper.writeValueAsString(test);
		assertThatJson(value).when(Option.IGNORING_ARRAY_ORDER)
			.when(Option.IGNORING_EXTRA_ARRAY_ITEMS)
			.isObject()
			.isEqualTo(
					json("""
							{"type":"resource","resource":{"uri":"resource://test","mimeType":"text/plain","text":"Sample resource content"}}"""));
	}

	@Test
	void testEmbeddedResourceDeserialization() throws Exception {
		EmbeddedResource embeddedResource = mapper.readValue(
				"""
						{"type":"resource","resource":{"uri":"resource://test","mimeType":"text/plain","text":"Sample resource content"}}""",
				EmbeddedResource.class);
		assertThat(embeddedResource).isNotNull();
		assertThat(embeddedResource.getType()).isEqualTo("resource");
		assertThat(embeddedResource.getResource()).isNotNull();
		assertThat(embeddedResource.getResource().getUri()).isEqualTo("resource://test");
		assertThat(embeddedResource.getResource().getMimeType()).isEqualTo("text/plain");
		assertThat(((TextResourceContents) embeddedResource.getResource()).getText())
			.isEqualTo("Sample resource content");
	}

	@Test
	void testEmbeddedResourceWithBlobContents() throws Exception {
		BlobResourceContents resourceContents = new BlobResourceContents("resource://test", "application/octet-stream",
				"base64encodedblob");

		EmbeddedResource test = new EmbeddedResource(null, null, resourceContents);

		String value = mapper.writeValueAsString(test);
		assertThatJson(value).when(Option.IGNORING_ARRAY_ORDER)
			.when(Option.IGNORING_EXTRA_ARRAY_ITEMS)
			.isObject()
			.isEqualTo(
					json("""
							{"type":"resource","resource":{"uri":"resource://test","mimeType":"application/octet-stream","blob":"base64encodedblob"}}"""));
	}

	@Test
	void testEmbeddedResourceWithBlobContentsDeserialization() throws Exception {
		EmbeddedResource embeddedResource = mapper.readValue(
				"""
						{"type":"resource","resource":{"uri":"resource://test","mimeType":"application/octet-stream","blob":"base64encodedblob"}}""",
				EmbeddedResource.class);
		assertThat(embeddedResource).isNotNull();
		assertThat(embeddedResource.getType()).isEqualTo("resource");
		assertThat(embeddedResource.getResource()).isNotNull();
		assertThat(embeddedResource.getResource().getUri()).isEqualTo("resource://test");
		assertThat(embeddedResource.getResource().getMimeType()).isEqualTo("application/octet-stream");
		assertThat(((BlobResourceContents) embeddedResource.getResource()).getBlob()).isEqualTo("base64encodedblob");
	}

	// JSON-RPC Message Types Tests

	@Test
	void testJSONRPCRequest() throws Exception {
		Map<String, Object> params = new HashMap<>();
		params.put("key", "value");

		JSONRPCRequest request = new JSONRPCRequest(McpSchema.JSONRPC_VERSION, "method_name", 1, params);

		String value = mapper.writeValueAsString(request);
		assertThatJson(value).when(Option.IGNORING_ARRAY_ORDER)
			.when(Option.IGNORING_EXTRA_ARRAY_ITEMS)
			.isObject()
			.isEqualTo(json("""
					{"jsonrpc":"2.0","method":"method_name","id":1,"params":{"key":"value"}}"""));
	}

	@Test
	void testJSONRPCNotification() throws Exception {
		Map<String, Object> params = new HashMap<>();
		params.put("key", "value");

		JSONRPCNotification notification = new JSONRPCNotification(McpSchema.JSONRPC_VERSION, "notification_method",
				params);

		String value = mapper.writeValueAsString(notification);
		assertThatJson(value).when(Option.IGNORING_ARRAY_ORDER)
			.when(Option.IGNORING_EXTRA_ARRAY_ITEMS)
			.isObject()
			.isEqualTo(json("""
					{"jsonrpc":"2.0","method":"notification_method","params":{"key":"value"}}"""));
	}

	@Test
	void testJSONRPCResponse() throws Exception {
		Map<String, Object> result = new HashMap<>();
		result.put("result_key", "result_value");

		JSONRPCResponse response = new JSONRPCResponse(McpSchema.JSONRPC_VERSION, 1, result, null);

		String value = mapper.writeValueAsString(response);
		assertThatJson(value).when(Option.IGNORING_ARRAY_ORDER)
			.when(Option.IGNORING_EXTRA_ARRAY_ITEMS)
			.isObject()
			.isEqualTo(json("""
					{"jsonrpc":"2.0","id":1,"result":{"result_key":"result_value"}}"""));
	}

	@Test
	void testJSONRPCResponseWithError() throws Exception {
		JSONRPCResponse.JSONRPCError error = new JSONRPCResponse.JSONRPCError(ErrorCodes.INVALID_REQUEST,
				"Invalid request", null);

		JSONRPCResponse response = new JSONRPCResponse(McpSchema.JSONRPC_VERSION, 1, null, error);

		String value = mapper.writeValueAsString(response);
		assertThatJson(value).when(Option.IGNORING_ARRAY_ORDER)
			.when(Option.IGNORING_EXTRA_ARRAY_ITEMS)
			.isObject()
			.isEqualTo(json("""
					{"jsonrpc":"2.0","id":1,"error":{"code":-32600,"message":"Invalid request"}}"""));
	}

	// Initialization Tests

	@Test
	void testInitializeRequest() throws Exception {
		ClientCapabilities capabilities = ClientCapabilities.builder().roots(true).sampling().build();

		Implementation clientInfo = new Implementation("test-client", "1.0.0");

		InitializeRequest request = new InitializeRequest("2024-11-05", capabilities, clientInfo);

		String value = mapper.writeValueAsString(request);
		assertThatJson(value).when(Option.IGNORING_ARRAY_ORDER)
			.when(Option.IGNORING_EXTRA_ARRAY_ITEMS)
			.isObject()
			.isEqualTo(
					json("""
							{"protocolVersion":"2024-11-05","capabilities":{"roots":{"listChanged":true},"sampling":{}},"clientInfo":{"name":"test-client","version":"1.0.0"}}"""));
	}

	@Test
	void testInitializeResult() throws Exception {
		ServerCapabilities capabilities = ServerCapabilities.builder()
			.logging()
			.prompts(true)
			.resources(true, true)
			.tools(true)
			.build();

		Implementation serverInfo = new Implementation("test-server", "1.0.0");

		InitializeResult result = new InitializeResult("2024-11-05", capabilities, serverInfo,
				"Server initialized successfully");

		String value = mapper.writeValueAsString(result);
		assertThatJson(value).when(Option.IGNORING_ARRAY_ORDER)
			.when(Option.IGNORING_EXTRA_ARRAY_ITEMS)
			.isObject()
			.isEqualTo(
					json("""
							{"protocolVersion":"2024-11-05","capabilities":{"logging":{},"prompts":{"listChanged":true},"resources":{"subscribe":true,"listChanged":true},"tools":{"listChanged":true}},"serverInfo":{"name":"test-server","version":"1.0.0"},"instructions":"Server initialized successfully"}"""));
	}

	// Resource Tests

	@Test
	void testResource() throws Exception {
		Annotations annotations = new Annotations(Arrays.asList(Role.USER, Role.ASSISTANT), 0.8);

		Resource resource = new Resource("resource://test", "Test Resource", "A test resource", "text/plain",
				annotations);

		String value = mapper.writeValueAsString(resource);
		assertThatJson(value).when(Option.IGNORING_ARRAY_ORDER)
			.when(Option.IGNORING_EXTRA_ARRAY_ITEMS)
			.isObject()
			.isEqualTo(
					json("""
							{"uri":"resource://test","name":"Test Resource","description":"A test resource","mimeType":"text/plain","annotations":{"audience":["user","assistant"],"priority":0.8}}"""));
	}

	@Test
	void testResourceTemplate() throws Exception {
		Annotations annotations = new Annotations(Arrays.asList(Role.USER), 0.5);

		ResourceTemplate template = new ResourceTemplate("resource://{param}/test", "Test Template",
				"A test resource template", "text/plain", annotations);

		String value = mapper.writeValueAsString(template);
		assertThatJson(value).when(Option.IGNORING_ARRAY_ORDER)
			.when(Option.IGNORING_EXTRA_ARRAY_ITEMS)
			.isObject()
			.isEqualTo(
					json("""
							{"uriTemplate":"resource://{param}/test","name":"Test Template","description":"A test resource template","mimeType":"text/plain","annotations":{"audience":["user"],"priority":0.5}}"""));
	}

	@Test
	void testListResourcesResult() throws Exception {
		Resource resource1 = new Resource("resource://test1", "Test Resource 1", "First test resource", "text/plain",
				null);

		Resource resource2 = new Resource("resource://test2", "Test Resource 2", "Second test resource",
				"application/json", null);

		ListResourcesResult result = new ListResourcesResult(Arrays.asList(resource1, resource2), "next-cursor");

		String value = mapper.writeValueAsString(result);
		assertThatJson(value).when(Option.IGNORING_ARRAY_ORDER)
			.when(Option.IGNORING_EXTRA_ARRAY_ITEMS)
			.isObject()
			.isEqualTo(
					json("""
							{"resources":[{"uri":"resource://test1","name":"Test Resource 1","description":"First test resource","mimeType":"text/plain"},{"uri":"resource://test2","name":"Test Resource 2","description":"Second test resource","mimeType":"application/json"}],"nextCursor":"next-cursor"}"""));
	}

	@Test
	void testListResourceTemplatesResult() throws Exception {
		ResourceTemplate template1 = new ResourceTemplate("resource://{param}/test1", "Test Template 1",
				"First test template", "text/plain", null);

		ResourceTemplate template2 = new ResourceTemplate("resource://{param}/test2", "Test Template 2",
				"Second test template", "application/json", null);

		ListResourceTemplatesResult result = new ListResourceTemplatesResult(Arrays.asList(template1, template2),
				"next-cursor");

		String value = mapper.writeValueAsString(result);
		assertThatJson(value).when(Option.IGNORING_ARRAY_ORDER)
			.when(Option.IGNORING_EXTRA_ARRAY_ITEMS)
			.isObject()
			.isEqualTo(
					json("""
							{"resourceTemplates":[{"uriTemplate":"resource://{param}/test1","name":"Test Template 1","description":"First test template","mimeType":"text/plain"},{"uriTemplate":"resource://{param}/test2","name":"Test Template 2","description":"Second test template","mimeType":"application/json"}],"nextCursor":"next-cursor"}"""));
	}

	@Test
	void testReadResourceRequest() throws Exception {
		ReadResourceRequest request = new ReadResourceRequest("resource://test");

		String value = mapper.writeValueAsString(request);
		assertThatJson(value).when(Option.IGNORING_ARRAY_ORDER)
			.when(Option.IGNORING_EXTRA_ARRAY_ITEMS)
			.isObject()
			.isEqualTo(json("""
					{"uri":"resource://test"}"""));
	}

	@Test
	void testReadResourceResult() throws Exception {
		TextResourceContents contents1 = new TextResourceContents("resource://test1", "text/plain",
				"Sample text content");

		BlobResourceContents contents2 = new BlobResourceContents("resource://test2", "application/octet-stream",
				"base64encodedblob");

		ReadResourceResult result = new ReadResourceResult(Arrays.asList(contents1, contents2));

		String value = mapper.writeValueAsString(result);
		assertThatJson(value).when(Option.IGNORING_ARRAY_ORDER)
			.when(Option.IGNORING_EXTRA_ARRAY_ITEMS)
			.isObject()
			.isEqualTo(
					json("""
							{"contents":[{"uri":"resource://test1","mimeType":"text/plain","text":"Sample text content"},{"uri":"resource://test2","mimeType":"application/octet-stream","blob":"base64encodedblob"}]}"""));
	}

	// Prompt Tests

	@Test
	void testPrompt() throws Exception {
		PromptArgument arg1 = new PromptArgument("arg1", "First argument", true);

		PromptArgument arg2 = new PromptArgument("arg2", "Second argument", false);

		Prompt prompt = new Prompt("test-prompt", "A test prompt", Arrays.asList(arg1, arg2));

		String value = mapper.writeValueAsString(prompt);
		assertThatJson(value).when(Option.IGNORING_ARRAY_ORDER)
			.when(Option.IGNORING_EXTRA_ARRAY_ITEMS)
			.isObject()
			.isEqualTo(
					json("""
							{"name":"test-prompt","description":"A test prompt","arguments":[{"name":"arg1","description":"First argument","required":true},{"name":"arg2","description":"Second argument","required":false}]}"""));
	}

	@Test
	void testPromptMessage() throws Exception {
		TextContent content = new TextContent("Hello, world!");

		PromptMessage message = new PromptMessage(Role.USER, content);

		String value = mapper.writeValueAsString(message);
		assertThatJson(value).when(Option.IGNORING_ARRAY_ORDER)
			.when(Option.IGNORING_EXTRA_ARRAY_ITEMS)
			.isObject()
			.isEqualTo(json("""
					{"role":"user","content":{"type":"text","text":"Hello, world!"}}"""));
	}

	@Test
	void testListPromptsResult() throws Exception {
		PromptArgument arg = new PromptArgument("arg", "An argument", true);

		Prompt prompt1 = new Prompt("prompt1", "First prompt", Collections.singletonList(arg));

		Prompt prompt2 = new Prompt("prompt2", "Second prompt", Collections.emptyList());

		ListPromptsResult result = new ListPromptsResult(Arrays.asList(prompt1, prompt2), "next-cursor");

		String value = mapper.writeValueAsString(result);
		assertThatJson(value).when(Option.IGNORING_ARRAY_ORDER)
			.when(Option.IGNORING_EXTRA_ARRAY_ITEMS)
			.isObject()
			.isEqualTo(
					json("""
							{"prompts":[{"name":"prompt1","description":"First prompt","arguments":[{"name":"arg","description":"An argument","required":true}]},{"name":"prompt2","description":"Second prompt","arguments":[]}],"nextCursor":"next-cursor"}"""));
	}

	@Test
	void testGetPromptRequest() throws Exception {
		Map<String, Object> arguments = new HashMap<>();
		arguments.put("arg1", "value1");
		arguments.put("arg2", 42);

		GetPromptRequest request = new GetPromptRequest("test-prompt", arguments);

		assertThat(mapper.readValue("""
				{"name":"test-prompt","arguments":{"arg1":"value1","arg2":42}}""", GetPromptRequest.class))
			.isEqualTo(request);
	}

	@Test
	void testGetPromptResult() throws Exception {
		TextContent content1 = new TextContent("System message");
		TextContent content2 = new TextContent("User message");

		PromptMessage message1 = new PromptMessage(Role.ASSISTANT, content1);

		PromptMessage message2 = new PromptMessage(Role.USER, content2);

		GetPromptResult result = new GetPromptResult("A test prompt result", Arrays.asList(message1, message2));

		String value = mapper.writeValueAsString(result);

		assertThatJson(value).when(Option.IGNORING_ARRAY_ORDER)
			.when(Option.IGNORING_EXTRA_ARRAY_ITEMS)
			.isObject()
			.isEqualTo(
					json("""
							{"description":"A test prompt result","messages":[{"role":"assistant","content":{"type":"text","text":"System message"}},{"role":"user","content":{"type":"text","text":"User message"}}]}"""));
	}

	// Tool Tests

	@Test
	void testJsonSchema() throws Exception {
		String schemaJson = """
				{
					"type": "object",
					"properties": {
						"name": {
							"type": "string"
						},
						"address": {
							"$ref": "#/$defs/Address"
						}
					},
					"required": ["name"],
					"$defs": {
						"Address": {
							"type": "object",
							"properties": {
								"street": {"type": "string"},
								"city": {"type": "string"}
							},
							"required": ["street", "city"]
						}
					}
				}
				""";

		// Deserialize the original string to a JsonSchema object
		JsonSchema schema = mapper.readValue(schemaJson, JsonSchema.class);

		// Serialize the object back to a string
		String serialized = mapper.writeValueAsString(schema);

		// Deserialize again
		JsonSchema deserialized = mapper.readValue(serialized, JsonSchema.class);

		// Serialize one more time and compare with the first serialization
		String serializedAgain = mapper.writeValueAsString(deserialized);

		// The two serialized strings should be the same
		assertThatJson(serializedAgain).when(Option.IGNORING_ARRAY_ORDER).isEqualTo(json(serialized));
	}

	@Test
	void testJsonSchemaWithDefinitions() throws Exception {
		String schemaJson = """
				{
					"type": "object",
					"properties": {
						"name": {
							"type": "string"
						},
						"address": {
							"$ref": "#/definitions/Address"
						}
					},
					"required": ["name"],
					"definitions": {
						"Address": {
							"type": "object",
							"properties": {
								"street": {"type": "string"},
								"city": {"type": "string"}
							},
							"required": ["street", "city"]
						}
					}
				}
				""";

		// Deserialize the original string to a JsonSchema object
		JsonSchema schema = mapper.readValue(schemaJson, JsonSchema.class);

		// Serialize the object back to a string
		String serialized = mapper.writeValueAsString(schema);

		// Deserialize again
		JsonSchema deserialized = mapper.readValue(serialized, JsonSchema.class);

		// Serialize one more time and compare with the first serialization
		String serializedAgain = mapper.writeValueAsString(deserialized);

		// The two serialized strings should be the same
		assertThatJson(serializedAgain).when(Option.IGNORING_ARRAY_ORDER).isEqualTo(json(serialized));
	}

	@Test
	void testTool() throws Exception {
		String schemaJson = """
				{
					"type": "object",
					"properties": {
						"name": {
							"type": "string"
						},
						"value": {
							"type": "number"
						}
					},
					"required": ["name"]
				}
				""";

		Tool tool = new Tool("test-tool", "A test tool", schemaJson);

		String value = mapper.writeValueAsString(tool);
		assertThatJson(value).when(Option.IGNORING_ARRAY_ORDER)
			.when(Option.IGNORING_EXTRA_ARRAY_ITEMS)
			.isObject()
			.isEqualTo(
					json("""
							{"name":"test-tool","description":"A test tool","inputSchema":{"type":"object","properties":{"name":{"type":"string"},"value":{"type":"number"}},"required":["name"]}}"""));
	}

	@Test
	void testToolWithComplexSchema() throws Exception {
		String complexSchemaJson = """
				{
					"type": "object",
					"$defs": {
						"Address": {
							"type": "object",
							"properties": {
								"street": {"type": "string"},
								"city": {"type": "string"}
							},
							"required": ["street", "city"]
						}
					},
					"properties": {
						"name": {"type": "string"},
						"shippingAddress": {"$ref": "#/$defs/Address"}
					},
					"required": ["name", "shippingAddress"]
				}
				""";

		Tool tool = new Tool("addressTool", "Handles addresses", complexSchemaJson);

		// Serialize the tool to a string
		String serialized = mapper.writeValueAsString(tool);

		// Deserialize back to a Tool object
		Tool deserializedTool = mapper.readValue(serialized, Tool.class);

		// Serialize again and compare with first serialization
		String serializedAgain = mapper.writeValueAsString(deserializedTool);

		// The two serialized strings should be the same
		assertThatJson(serializedAgain).when(Option.IGNORING_ARRAY_ORDER).isEqualTo(json(serialized));

		// Just verify the basic structure was preserved
		assertThat(deserializedTool.getInputSchema().getDefs()).isNotNull();
		assertThat(deserializedTool.getInputSchema().getDefs()).containsKey("Address");
	}

	@Test
	void testCallToolRequest() throws Exception {
		Map<String, Object> arguments = new HashMap<>();
		arguments.put("name", "test");
		arguments.put("value", 42);

		CallToolRequest request = new CallToolRequest("test-tool", arguments);

		String value = mapper.writeValueAsString(request);

		assertThatJson(value).when(Option.IGNORING_ARRAY_ORDER)
			.when(Option.IGNORING_EXTRA_ARRAY_ITEMS)
			.isObject()
			.isEqualTo(json("""
					{"name":"test-tool","arguments":{"name":"test","value":42}}"""));
	}

	@Test
	void testCallToolRequestJsonArguments() throws Exception {

		CallToolRequest request = new CallToolRequest("test-tool", """
				{
					"name": "test",
					"value": 42
				}
				""");

		String value = mapper.writeValueAsString(request);

		assertThatJson(value).when(Option.IGNORING_ARRAY_ORDER)
			.when(Option.IGNORING_EXTRA_ARRAY_ITEMS)
			.isObject()
			.isEqualTo(json("""
					{"name":"test-tool","arguments":{"name":"test","value":42}}"""));
	}

	@Test
	void testCallToolResult() throws Exception {
		TextContent content = new TextContent("Tool execution result");

		CallToolResult result = new CallToolResult(Collections.singletonList(content), false);

		String value = mapper.writeValueAsString(result);

		assertThatJson(value).when(Option.IGNORING_ARRAY_ORDER)
			.when(Option.IGNORING_EXTRA_ARRAY_ITEMS)
			.isObject()
			.isEqualTo(json("""
					{"content":[{"type":"text","text":"Tool execution result"}],"isError":false}"""));
	}

	@Test
	void testCallToolResultBuilder() throws Exception {
		CallToolResult result = CallToolResult.builder().addTextContent("Tool execution result").isError(false).build();

		String value = mapper.writeValueAsString(result);

		assertThatJson(value).when(Option.IGNORING_ARRAY_ORDER)
			.when(Option.IGNORING_EXTRA_ARRAY_ITEMS)
			.isObject()
			.isEqualTo(json("""
					{"content":[{"type":"text","text":"Tool execution result"}],"isError":false}"""));
	}

	@Test
	void testCallToolResultBuilderWithMultipleContents() throws Exception {
		TextContent textContent = new TextContent("Text result");
		ImageContent imageContent = new ImageContent(null, null, "base64data", "image/png");

		CallToolResult result = CallToolResult.builder()
			.addContent(textContent)
			.addContent(imageContent)
			.isError(false)
			.build();

		String value = mapper.writeValueAsString(result);

		assertThatJson(value).when(Option.IGNORING_ARRAY_ORDER)
			.when(Option.IGNORING_EXTRA_ARRAY_ITEMS)
			.isObject()
			.isEqualTo(
					json("""
							{"content":[{"type":"text","text":"Text result"},{"type":"image","data":"base64data","mimeType":"image/png"}],"isError":false}"""));
	}

	@Test
	void testCallToolResultBuilderWithContentList() throws Exception {
		TextContent textContent = new TextContent("Text result");
		ImageContent imageContent = new ImageContent(null, null, "base64data", "image/png");
		List<Content> contents = Arrays.asList(textContent, imageContent);

		CallToolResult result = CallToolResult.builder().content(contents).isError(true).build();

		String value = mapper.writeValueAsString(result);

		assertThatJson(value).when(Option.IGNORING_ARRAY_ORDER)
			.when(Option.IGNORING_EXTRA_ARRAY_ITEMS)
			.isObject()
			.isEqualTo(
					json("""
							{"content":[{"type":"text","text":"Text result"},{"type":"image","data":"base64data","mimeType":"image/png"}],"isError":true}"""));
	}

	@Test
	void testCallToolResultBuilderWithErrorResult() throws Exception {
		CallToolResult result = CallToolResult.builder()
			.addTextContent("Error: Operation failed")
			.isError(true)
			.build();

		String value = mapper.writeValueAsString(result);

		assertThatJson(value).when(Option.IGNORING_ARRAY_ORDER)
			.when(Option.IGNORING_EXTRA_ARRAY_ITEMS)
			.isObject()
			.isEqualTo(json("""
					{"content":[{"type":"text","text":"Error: Operation failed"}],"isError":true}"""));
	}

	@Test
	void testCallToolResultStringConstructor() throws Exception {
		// Test the existing string constructor alongside the builder
		CallToolResult result1 = new CallToolResult("Simple result", false);
		CallToolResult result2 = CallToolResult.builder().addTextContent("Simple result").isError(false).build();

		String value1 = mapper.writeValueAsString(result1);
		String value2 = mapper.writeValueAsString(result2);

		// Both should produce the same JSON
		assertThat(value1).isEqualTo(value2);
		assertThatJson(value1).when(Option.IGNORING_ARRAY_ORDER)
			.when(Option.IGNORING_EXTRA_ARRAY_ITEMS)
			.isObject()
			.isEqualTo(json("""
					{"content":[{"type":"text","text":"Simple result"}],"isError":false}"""));
	}

	// Sampling Tests

	@Test
	void testCreateMessageRequest() throws Exception {
		TextContent content = new TextContent("User message");

		SamplingMessage message = new SamplingMessage(Role.USER, content);

		ModelHint hint = new ModelHint("gpt-4");

		ModelPreferences preferences = new ModelPreferences(Collections.singletonList(hint), 0.3, 0.7, 0.9);

		Map<String, Object> metadata = new HashMap<>();
		metadata.put("session", "test-session");

		CreateMessageRequest request = CreateMessageRequest.builder()
			.messages(Collections.singletonList(message))
			.modelPreferences(preferences)
			.systemPrompt("You are a helpful assistant")
			.includeContext(CreateMessageRequest.ContextInclusionStrategy.THIS_SERVER)
			.temperature(0.7)
			.maxTokens(1000)
			.stopSequences(Arrays.asList("STOP", "END"))
			.metadata(metadata)
			.build();

		String value = mapper.writeValueAsString(request);

		assertThatJson(value).when(Option.IGNORING_ARRAY_ORDER)
			.when(Option.IGNORING_EXTRA_ARRAY_ITEMS)
			.isObject()
			.isEqualTo(
					json("""
							{"messages":[{"role":"user","content":{"type":"text","text":"User message"}}],"modelPreferences":{"hints":[{"name":"gpt-4"}],"costPriority":0.3,"speedPriority":0.7,"intelligencePriority":0.9},"systemPrompt":"You are a helpful assistant","includeContext":"thisServer","temperature":0.7,"maxTokens":1000,"stopSequences":["STOP","END"],"metadata":{"session":"test-session"}}"""));
	}

	@Test
	void testCreateMessageResult() throws Exception {
		TextContent content = new TextContent("Assistant response");

		CreateMessageResult result = CreateMessageResult.builder()
			.role(Role.ASSISTANT)
			.content(content)
			.model("gpt-4")
			.stopReason(CreateMessageResult.StopReason.END_TURN)
			.build();

		String value = mapper.writeValueAsString(result);

		assertThatJson(value).when(Option.IGNORING_ARRAY_ORDER)
			.when(Option.IGNORING_EXTRA_ARRAY_ITEMS)
			.isObject()
			.isEqualTo(
					json("""
							{"role":"assistant","content":{"type":"text","text":"Assistant response"},"model":"gpt-4","stopReason":"endTurn"}"""));
	}

	// Roots Tests

	@Test
	void testRoot() throws Exception {
		Root root = new Root("file:///path/to/root", "Test Root");

		String value = mapper.writeValueAsString(root);
		assertThatJson(value).when(Option.IGNORING_ARRAY_ORDER)
			.when(Option.IGNORING_EXTRA_ARRAY_ITEMS)
			.isObject()
			.isEqualTo(json("""
					{"uri":"file:///path/to/root","name":"Test Root"}"""));
	}

	@Test
	void testListRootsResult() throws Exception {
		Root root1 = new Root("file:///path/to/root1", "First Root");

		Root root2 = new Root("file:///path/to/root2", "Second Root");

		ListRootsResult result = new ListRootsResult(Arrays.asList(root1, root2));

		String value = mapper.writeValueAsString(result);

		assertThatJson(value).when(Option.IGNORING_ARRAY_ORDER)
			.when(Option.IGNORING_EXTRA_ARRAY_ITEMS)
			.isObject()
			.isEqualTo(
					json("""
							{"roots":[{"uri":"file:///path/to/root1","name":"First Root"},{"uri":"file:///path/to/root2","name":"Second Root"}]}"""));

	}

}
