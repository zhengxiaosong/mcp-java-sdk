/*
 * Copyright 2024-2024 the original author or authors.
 */

package io.modelcontextprotocol.server;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;

import io.modelcontextprotocol.spec.autocomplete.CompleteReference;
import io.modelcontextprotocol.spec.autocomplete.CompleteRequest;
import io.modelcontextprotocol.spec.autocomplete.CompleteResult;
import io.modelcontextprotocol.spec.common.Root;
import io.modelcontextprotocol.spec.initialization.Implementation;
import io.modelcontextprotocol.spec.initialization.ServerCapabilities;
import io.modelcontextprotocol.spec.prompt.GetPromptRequest;
import io.modelcontextprotocol.spec.prompt.GetPromptResult;
import io.modelcontextprotocol.spec.prompt.Prompt;
import io.modelcontextprotocol.spec.resource.ReadResourceRequest;
import io.modelcontextprotocol.spec.resource.ReadResourceResult;
import io.modelcontextprotocol.spec.resource.Resource;
import io.modelcontextprotocol.spec.resource.ResourceTemplate;
import io.modelcontextprotocol.spec.tool.CallToolResult;
import io.modelcontextprotocol.spec.tool.Tool;
import io.modelcontextprotocol.util.Assert;
import io.modelcontextprotocol.util.Utils;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

/**
 * MCP server features specification that a particular server can choose to support.
 *
 * @author Dariusz Jędrzejczyk
 * @author Jihoon Kim
 */
public class McpServerFeatures {

	/**
	 * Asynchronous server features specification.
	 *
	 * @param serverInfo The server implementation details
	 * @param serverCapabilities The server capabilities
	 * @param tools The list of tool specifications
	 * @param resources The map of resource specifications
	 * @param resourceTemplates The list of resource templates
	 * @param prompts The map of prompt specifications
	 * @param rootsChangeConsumers The list of consumers that will be notified when the
	 * roots list changes
	 * @param instructions The server instructions text
	 */
	public static class Async {

		private final Implementation serverInfo;

		private final ServerCapabilities serverCapabilities;

		private final List<McpServerFeatures.AsyncToolSpecification> tools;

		private final Map<String, AsyncResourceSpecification> resources;

		private final List<ResourceTemplate> resourceTemplates;

		private final Map<String, McpServerFeatures.AsyncPromptSpecification> prompts;

		private final Map<CompleteReference, McpServerFeatures.AsyncCompletionSpecification> completions;

		private final List<BiFunction<McpAsyncServerExchange, List<Root>, Mono<Void>>> rootsChangeConsumers;

		private final String instructions;

		/**
		 * Create an instance and validate the arguments.
		 * @param serverInfo The server implementation details
		 * @param serverCapabilities The server capabilities
		 * @param tools The list of tool specifications
		 * @param resources The map of resource specifications
		 * @param resourceTemplates The list of resource templates
		 * @param prompts The map of prompt specifications
		 * @param rootsChangeConsumers The list of consumers that will be notified when
		 * the roots list changes
		 * @param instructions The server instructions text
		 */
		Async(Implementation serverInfo, ServerCapabilities serverCapabilities,
				List<McpServerFeatures.AsyncToolSpecification> tools, Map<String, AsyncResourceSpecification> resources,
				List<ResourceTemplate> resourceTemplates,
				Map<String, McpServerFeatures.AsyncPromptSpecification> prompts,
				Map<CompleteReference, McpServerFeatures.AsyncCompletionSpecification> completions,
				List<BiFunction<McpAsyncServerExchange, List<Root>, Mono<Void>>> rootsChangeConsumers,
				String instructions) {

			Assert.notNull(serverInfo, "Server info must not be null");

			this.serverInfo = serverInfo;
			this.serverCapabilities = (serverCapabilities != null) ? serverCapabilities : new ServerCapabilities(null, // completions
					null, // experimental
					new ServerCapabilities.LoggingCapabilities(), // Enable
																	// logging
																	// by
																	// default
					!Utils.isEmpty(prompts) ? new ServerCapabilities.PromptCapabilities(false) : null,
					!Utils.isEmpty(resources) ? new ServerCapabilities.ResourceCapabilities(false, false) : null,
					!Utils.isEmpty(tools) ? new ServerCapabilities.ToolCapabilities(false) : null);

			this.tools = (tools != null) ? tools : List.of();
			this.resources = (resources != null) ? resources : Map.of();
			this.resourceTemplates = (resourceTemplates != null) ? resourceTemplates : List.of();
			this.prompts = (prompts != null) ? prompts : Map.of();
			this.completions = (completions != null) ? completions : Map.of();
			this.rootsChangeConsumers = (rootsChangeConsumers != null) ? rootsChangeConsumers : List.of();
			this.instructions = instructions;
		}

		public Implementation serverInfo() {
			return this.serverInfo;
		}

		public ServerCapabilities serverCapabilities() {
			return this.serverCapabilities;
		}

		public List<McpServerFeatures.AsyncToolSpecification> tools() {
			return this.tools;
		}

		public Map<String, AsyncResourceSpecification> resources() {
			return this.resources;
		}

		public List<ResourceTemplate> resourceTemplates() {
			return this.resourceTemplates;
		}

		public Map<String, McpServerFeatures.AsyncPromptSpecification> prompts() {
			return this.prompts;
		}

		public Map<CompleteReference, McpServerFeatures.AsyncCompletionSpecification> completions() {
			return this.completions;
		}

		public List<BiFunction<McpAsyncServerExchange, List<Root>, Mono<Void>>> rootsChangeConsumers() {
			return this.rootsChangeConsumers;
		}

		public String instructions() {
			return this.instructions;
		}

		/**
		 * Convert a synchronous specification into an asynchronous one and provide
		 * blocking code offloading to prevent accidental blocking of the non-blocking
		 * transport.
		 * @param syncSpec a potentially blocking, synchronous specification.
		 * @return a specification which is protected from blocking calls specified by the
		 * user.
		 */
		static Async fromSync(Sync syncSpec) {
			List<McpServerFeatures.AsyncToolSpecification> tools = new ArrayList<>();
			for (var tool : syncSpec.tools()) {
				tools.add(AsyncToolSpecification.fromSync(tool));
			}

			Map<String, AsyncResourceSpecification> resources = new HashMap<>();
			syncSpec.resources().forEach((key, resource) -> {
				resources.put(key, AsyncResourceSpecification.fromSync(resource));
			});

			Map<String, AsyncPromptSpecification> prompts = new HashMap<>();
			syncSpec.prompts().forEach((key, prompt) -> {
				prompts.put(key, AsyncPromptSpecification.fromSync(prompt));
			});

			Map<CompleteReference, McpServerFeatures.AsyncCompletionSpecification> completions = new HashMap<>();
			syncSpec.completions().forEach((key, completion) -> {
				completions.put(key, AsyncCompletionSpecification.fromSync(completion));
			});

			List<BiFunction<McpAsyncServerExchange, List<Root>, Mono<Void>>> rootChangeConsumers = new ArrayList<>();

			for (var rootChangeConsumer : syncSpec.rootsChangeConsumers()) {
				rootChangeConsumers.add((exchange, list) -> Mono
					.<Void>fromRunnable(() -> rootChangeConsumer.accept(new McpSyncServerExchange(exchange), list))
					.subscribeOn(Schedulers.boundedElastic()));
			}

			return new Async(syncSpec.serverInfo(), syncSpec.serverCapabilities(), tools, resources,
					syncSpec.resourceTemplates(), prompts, completions, rootChangeConsumers, syncSpec.instructions());
		}

	}

	/**
	 * Synchronous server features specification.
	 *
	 * @param serverInfo The server implementation details
	 * @param serverCapabilities The server capabilities
	 * @param tools The list of tool specifications
	 * @param resources The map of resource specifications
	 * @param resourceTemplates The list of resource templates
	 * @param prompts The map of prompt specifications
	 * @param rootsChangeConsumers The list of consumers that will be notified when the
	 * roots list changes
	 * @param instructions The server instructions text
	 */
	public static class Sync {

		private final Implementation serverInfo;

		private final ServerCapabilities serverCapabilities;

		private final List<McpServerFeatures.SyncToolSpecification> tools;

		private final Map<String, McpServerFeatures.SyncResourceSpecification> resources;

		private final List<ResourceTemplate> resourceTemplates;

		private final Map<String, McpServerFeatures.SyncPromptSpecification> prompts;

		private final Map<CompleteReference, McpServerFeatures.SyncCompletionSpecification> completions;

		private final List<BiConsumer<McpSyncServerExchange, List<Root>>> rootsChangeConsumers;

		private final String instructions;

		/**
		 * Create an instance and validate the arguments.
		 * @param serverInfo The server implementation details
		 * @param serverCapabilities The server capabilities
		 * @param tools The list of tool specifications
		 * @param resources The map of resource specifications
		 * @param resourceTemplates The list of resource templates
		 * @param prompts The map of prompt specifications
		 * @param rootsChangeConsumers The list of consumers that will be notified when
		 * the roots list changes
		 * @param instructions The server instructions text
		 */
		Sync(Implementation serverInfo, ServerCapabilities serverCapabilities,
				List<McpServerFeatures.SyncToolSpecification> tools,
				Map<String, McpServerFeatures.SyncResourceSpecification> resources,
				List<ResourceTemplate> resourceTemplates,
				Map<String, McpServerFeatures.SyncPromptSpecification> prompts,
				Map<CompleteReference, McpServerFeatures.SyncCompletionSpecification> completions,
				List<BiConsumer<McpSyncServerExchange, List<Root>>> rootsChangeConsumers, String instructions) {

			Assert.notNull(serverInfo, "Server info must not be null");

			this.serverInfo = serverInfo;
			this.serverCapabilities = (serverCapabilities != null) ? serverCapabilities : new ServerCapabilities(null, // completions
					null, // experimental
					new ServerCapabilities.LoggingCapabilities(), // Enable
																	// logging
																	// by
																	// default
					!Utils.isEmpty(prompts) ? new ServerCapabilities.PromptCapabilities(false) : null,
					!Utils.isEmpty(resources) ? new ServerCapabilities.ResourceCapabilities(false, false) : null,
					!Utils.isEmpty(tools) ? new ServerCapabilities.ToolCapabilities(false) : null);

			this.tools = (tools != null) ? tools : new ArrayList<>();
			this.resources = (resources != null) ? resources : new HashMap<>();
			this.resourceTemplates = (resourceTemplates != null) ? resourceTemplates : new ArrayList<>();
			this.prompts = (prompts != null) ? prompts : new HashMap<>();
			this.completions = (completions != null) ? completions : new HashMap<>();
			this.rootsChangeConsumers = (rootsChangeConsumers != null) ? rootsChangeConsumers : new ArrayList<>();
			this.instructions = instructions;
		}

		public Implementation serverInfo() {
			return this.serverInfo;
		}

		public ServerCapabilities serverCapabilities() {
			return this.serverCapabilities;
		}

		public List<McpServerFeatures.SyncToolSpecification> tools() {
			return this.tools;
		}

		public Map<String, McpServerFeatures.SyncResourceSpecification> resources() {
			return this.resources;
		}

		public List<ResourceTemplate> resourceTemplates() {
			return this.resourceTemplates;
		}

		public Map<String, McpServerFeatures.SyncPromptSpecification> prompts() {
			return this.prompts;
		}

		public Map<CompleteReference, McpServerFeatures.SyncCompletionSpecification> completions() {
			return this.completions;
		}

		public List<BiConsumer<McpSyncServerExchange, List<Root>>> rootsChangeConsumers() {
			return this.rootsChangeConsumers;
		}

		public String instructions() {
			return this.instructions;
		}

	}

	public static final class AsyncToolSpecification {

		private final Tool tool;

		private final BiFunction<McpAsyncServerExchange, Map<String, Object>, Mono<CallToolResult>> call;

		public AsyncToolSpecification(Tool tool,
				BiFunction<McpAsyncServerExchange, Map<String, Object>, Mono<CallToolResult>> call) {
			this.tool = tool;
			this.call = call;
		}

		public Tool getTool() {
			return this.tool;
		}

		public BiFunction<McpAsyncServerExchange, Map<String, Object>, Mono<CallToolResult>> getCall() {
			return this.call;
		}

		static AsyncToolSpecification fromSync(SyncToolSpecification tool) {
			if (tool == null) {
				return null;
			}
			return new AsyncToolSpecification(tool.getTool(),
					(exchange, map) -> Mono
						.fromCallable(() -> tool.getCall().apply(new McpSyncServerExchange(exchange), map))
						.subscribeOn(Schedulers.boundedElastic()));
		}

	}

	/**
	 * Specification of a resource with its asynchronous handler function. Resources
	 * provide context to AI models by exposing data such as:
	 * <ul>
	 * <li>File contents
	 * <li>Database records
	 * <li>API responses
	 * <li>System information
	 * <li>Application state
	 * </ul>
	 *
	 * <p>
	 * Example resource specification: <pre>{@code
	 * new McpServerFeatures.AsyncResourceSpecification(
	 *     new Resource("docs", "Documentation files", "text/markdown"),
	 *     (exchange, request) ->
	 *         Mono.fromSupplier(() -> readFile(request.getPath()))
	 *             .map(ReadResourceResult::new)
	 * )
	 * }</pre>
	 *
	 * @param resource The resource definition including name, description, and MIME type
	 * @param readHandler The function that handles resource read requests. The function's
	 * first argument is an {@link McpAsyncServerExchange} upon which the server can
	 * interact with the connected client. The second arguments is a
	 * {@link io.modelcontextprotocol.spec.ReadResourceRequest}.
	 */
	public static class AsyncResourceSpecification {

		private final Resource resource;

		private final BiFunction<McpAsyncServerExchange, ReadResourceRequest, Mono<ReadResourceResult>> readHandler;

		public AsyncResourceSpecification(Resource resource,
				BiFunction<McpAsyncServerExchange, ReadResourceRequest, Mono<ReadResourceResult>> readHandler) {
			this.resource = resource;
			this.readHandler = readHandler;
		}

		public Resource resource() {
			return this.resource;
		}

		public BiFunction<McpAsyncServerExchange, ReadResourceRequest, Mono<ReadResourceResult>> readHandler() {
			return this.readHandler;
		}

		static AsyncResourceSpecification fromSync(SyncResourceSpecification resource) {
			// FIXME: This is temporary, proper validation should be implemented
			if (resource == null) {
				return null;
			}
			return new AsyncResourceSpecification(resource.resource(),
					(exchange, req) -> Mono
						.fromCallable(() -> resource.readHandler().apply(new McpSyncServerExchange(exchange), req))
						.subscribeOn(Schedulers.boundedElastic()));
		}

	}

	/**
	 * Specification of a prompt template with its asynchronous handler function. Prompts
	 * provide structured templates for AI model interactions, supporting:
	 * <ul>
	 * <li>Consistent message formatting
	 * <li>Parameter substitution
	 * <li>Context injection
	 * <li>Response formatting
	 * <li>Instruction templating
	 * </ul>
	 *
	 * <p>
	 * Example prompt specification: <pre>{@code
	 * new McpServerFeatures.AsyncPromptSpecification(
	 *     new Prompt("analyze", "Code analysis template"),
	 *     (exchange, request) -> {
	 *         String code = request.getArguments().get("code");
	 *         return Mono.just(new GetPromptResult(
	 *             "Analyze this code:\n\n" + code + "\n\nProvide feedback on:"
	 *         ));
	 *     }
	 * )
	 * }</pre>
	 *
	 * @param prompt The prompt definition including name and description
	 * @param promptHandler The function that processes prompt requests and returns
	 * formatted templates. The function's first argument is an
	 * {@link McpAsyncServerExchange} upon which the server can interact with the
	 * connected client. The second arguments is a
	 * {@link io.modelcontextprotocol.spec.GetPromptRequest}.
	 */
	public static class AsyncPromptSpecification {

		private final Prompt prompt;

		private final BiFunction<McpAsyncServerExchange, GetPromptRequest, Mono<GetPromptResult>> promptHandler;

		public AsyncPromptSpecification(Prompt prompt,
				BiFunction<McpAsyncServerExchange, GetPromptRequest, Mono<GetPromptResult>> promptHandler) {
			this.prompt = prompt;
			this.promptHandler = promptHandler;
		}

		public Prompt prompt() {
			return this.prompt;
		}

		public BiFunction<McpAsyncServerExchange, GetPromptRequest, Mono<GetPromptResult>> promptHandler() {
			return this.promptHandler;
		}

		static AsyncPromptSpecification fromSync(SyncPromptSpecification prompt) {
			// FIXME: This is temporary, proper validation should be implemented
			if (prompt == null) {
				return null;
			}
			return new AsyncPromptSpecification(prompt.prompt(),
					(exchange, req) -> Mono
						.fromCallable(() -> prompt.promptHandler().apply(new McpSyncServerExchange(exchange), req))
						.subscribeOn(Schedulers.boundedElastic()));
		}

	}

	/**
	 * Specification of a completion handler function with asynchronous execution support.
	 * Completions generate AI model outputs based on prompt or resource references and
	 * user-provided arguments. This abstraction enables:
	 * <ul>
	 * <li>Customizable response generation logic
	 * <li>Parameter-driven template expansion
	 * <li>Dynamic interaction with connected clients
	 * </ul>
	 *
	 * @param referenceKey The unique key representing the completion reference.
	 * @param completionHandler The asynchronous function that processes completion
	 * requests and returns results. The first argument is an
	 * {@link McpAsyncServerExchange} used to interact with the client. The second
	 * argument is a {@link io.modelcontextprotocol.spec.CompleteRequest}.
	 */
	public static class AsyncCompletionSpecification {

		private final CompleteReference referenceKey;

		private final BiFunction<McpAsyncServerExchange, CompleteRequest, Mono<CompleteResult>> completionHandler;

		public AsyncCompletionSpecification(CompleteReference referenceKey,
				BiFunction<McpAsyncServerExchange, CompleteRequest, Mono<CompleteResult>> completionHandler) {
			this.referenceKey = referenceKey;
			this.completionHandler = completionHandler;
		}

		public CompleteReference referenceKey() {
			return this.referenceKey;
		}

		public BiFunction<McpAsyncServerExchange, CompleteRequest, Mono<CompleteResult>> completionHandler() {
			return this.completionHandler;
		}

		/**
		 * Converts a synchronous {@link SyncCompletionSpecification} into an
		 * {@link AsyncCompletionSpecification} by wrapping the handler in a bounded
		 * elastic scheduler for safe non-blocking execution.
		 * @param completion the synchronous completion specification
		 * @return an asynchronous wrapper of the provided sync specification, or
		 * {@code null} if input is null
		 */
		static AsyncCompletionSpecification fromSync(SyncCompletionSpecification completion) {
			if (completion == null) {
				return null;
			}
			return new AsyncCompletionSpecification(completion.referenceKey(),
					(exchange, request) -> Mono.fromCallable(
							() -> completion.completionHandler().apply(new McpSyncServerExchange(exchange), request))
						.subscribeOn(Schedulers.boundedElastic()));
		}

	}

	public static final class SyncToolSpecification {

		private final Tool tool;

		private final BiFunction<McpSyncServerExchange, Map<String, Object>, CallToolResult> call;

		public SyncToolSpecification(Tool tool,
				BiFunction<McpSyncServerExchange, Map<String, Object>, CallToolResult> call) {
			this.tool = tool;
			this.call = call;
		}

		public Tool getTool() {
			return this.tool;
		}

		public BiFunction<McpSyncServerExchange, Map<String, Object>, CallToolResult> getCall() {
			return this.call;
		}

	}

	/**
	 * Specification of a resource with its synchronous handler function. Resources
	 * provide context to AI models by exposing data such as:
	 * <ul>
	 * <li>File contents
	 * <li>Database records
	 * <li>API responses
	 * <li>System information
	 * <li>Application state
	 * </ul>
	 *
	 * <p>
	 * Example resource specification: <pre>{@code
	 * new McpServerFeatures.SyncResourceSpecification(
	 *     new Resource("docs", "Documentation files", "text/markdown"),
	 *     (exchange, request) -> {
	 *         String content = readFile(request.getPath());
	 *         return new ReadResourceResult(content);
	 *     }
	 * )
	 * }</pre>
	 *
	 * @param resource The resource definition including name, description, and MIME type
	 * @param readHandler The function that handles resource read requests. The function's
	 * first argument is an {@link McpSyncServerExchange} upon which the server can
	 * interact with the connected client. The second arguments is a
	 * {@link io.modelcontextprotocol.spec.ReadResourceRequest}.
	 */
	public static class SyncResourceSpecification {

		private final Resource resource;

		private final BiFunction<McpSyncServerExchange, ReadResourceRequest, ReadResourceResult> readHandler;

		public SyncResourceSpecification(Resource resource,
				BiFunction<McpSyncServerExchange, ReadResourceRequest, ReadResourceResult> readHandler) {
			this.resource = resource;
			this.readHandler = readHandler;
		}

		public Resource resource() {
			return this.resource;
		}

		public BiFunction<McpSyncServerExchange, ReadResourceRequest, ReadResourceResult> readHandler() {
			return this.readHandler;
		}

	}

	/**
	 * Specification of a prompt template with its synchronous handler function. Prompts
	 * provide structured templates for AI model interactions, supporting:
	 * <ul>
	 * <li>Consistent message formatting
	 * <li>Parameter substitution
	 * <li>Context injection
	 * <li>Response formatting
	 * <li>Instruction templating
	 * </ul>
	 *
	 * <p>
	 * Example prompt specification: <pre>{@code
	 * new McpServerFeatures.SyncPromptSpecification(
	 *     new Prompt("analyze", "Code analysis template"),
	 *     (exchange, request) -> {
	 *         String code = request.getArguments().get("code");
	 *         return new GetPromptResult(
	 *             "Analyze this code:\n\n" + code + "\n\nProvide feedback on:"
	 *         );
	 *     }
	 * )
	 * }</pre>
	 *
	 * @param prompt The prompt definition including name and description
	 * @param promptHandler The function that processes prompt requests and returns
	 * formatted templates. The function's first argument is an
	 * {@link McpSyncServerExchange} upon which the server can interact with the connected
	 * client. The second arguments is a
	 * {@link io.modelcontextprotocol.spec.GetPromptRequest}.
	 */
	public static class SyncPromptSpecification {

		private final Prompt prompt;

		private final BiFunction<McpSyncServerExchange, GetPromptRequest, GetPromptResult> promptHandler;

		public SyncPromptSpecification(Prompt prompt,
				BiFunction<McpSyncServerExchange, GetPromptRequest, GetPromptResult> promptHandler) {
			this.prompt = prompt;
			this.promptHandler = promptHandler;
		}

		public Prompt prompt() {
			return this.prompt;
		}

		public BiFunction<McpSyncServerExchange, GetPromptRequest, GetPromptResult> promptHandler() {
			return this.promptHandler;
		}

	}

	/**
	 * Specification of a completion handler function with synchronous execution support.
	 *
	 * @param referenceKey The unique key representing the completion reference.
	 * @param completionHandler The synchronous function that processes completion
	 * requests and returns results. The first argument is an
	 * {@link McpSyncServerExchange} used to interact with the client. The second argument
	 * is a {@link io.modelcontextprotocol.spec.CompleteRequest}.
	 */
	public static class SyncCompletionSpecification {

		private final CompleteReference referenceKey;

		private final BiFunction<McpSyncServerExchange, CompleteRequest, CompleteResult> completionHandler;

		public SyncCompletionSpecification(CompleteReference referenceKey,
				BiFunction<McpSyncServerExchange, CompleteRequest, CompleteResult> completionHandler) {
			this.referenceKey = referenceKey;
			this.completionHandler = completionHandler;
		}

		public CompleteReference referenceKey() {
			return this.referenceKey;
		}

		public BiFunction<McpSyncServerExchange, CompleteRequest, CompleteResult> completionHandler() {
			return this.completionHandler;
		}

	}

	public interface RootsChangeHandler extends BiFunction<McpSyncServerExchange, List<Root>, Mono<Void>> {

	}

}
