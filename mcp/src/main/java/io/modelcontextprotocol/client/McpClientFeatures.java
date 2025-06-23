/*
 * Copyright 2024-2024 the original author or authors.
 */

package io.modelcontextprotocol.client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Function;

import io.modelcontextprotocol.spec.common.Root;
import io.modelcontextprotocol.spec.initialization.ClientCapabilities;
import io.modelcontextprotocol.spec.initialization.Implementation;
import io.modelcontextprotocol.spec.logging.LoggingMessageNotification;
import io.modelcontextprotocol.spec.prompt.Prompt;
import io.modelcontextprotocol.spec.resource.Resource;
import io.modelcontextprotocol.spec.sampling.CreateMessageRequest;
import io.modelcontextprotocol.spec.sampling.CreateMessageResult;
import io.modelcontextprotocol.spec.tool.Tool;
import io.modelcontextprotocol.util.Assert;
import io.modelcontextprotocol.util.Utils;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

/**
 * Representation of features and capabilities for Model Context Protocol (MCP) clients.
 * This class provides two record types for managing client features:
 * <ul>
 * <li>{@link Async} for non-blocking operations with Project Reactor's Mono responses
 * <li>{@link Sync} for blocking operations with direct responses
 * </ul>
 *
 * <p>
 * Each feature specification includes:
 * <ul>
 * <li>Client implementation information and capabilities
 * <li>Root URI mappings for resource access
 * <li>Change notification handlers for tools, resources, and prompts
 * <li>Logging message consumers
 * <li>Message sampling handlers for request processing
 * </ul>
 *
 * <p>
 * The class supports conversion between synchronous and asynchronous specifications
 * through the {@link Async#fromSync} method, which ensures proper handling of blocking
 * operations in non-blocking contexts by scheduling them on a bounded elastic scheduler.
 *
 * @author Dariusz Jędrzejczyk
 * @see McpClient
 * @see Implementation
 * @see ClientCapabilities
 */
class McpClientFeatures {

	/**
	 * Asynchronous client features specification providing the capabilities and request
	 * and notification handlers.
	 *
	 * @param clientInfo the client implementation information.
	 * @param clientCapabilities the client capabilities.
	 * @param roots the roots.
	 * @param toolsChangeConsumers the tools change consumers.
	 * @param resourcesChangeConsumers the resources change consumers.
	 * @param promptsChangeConsumers the prompts change consumers.
	 * @param loggingConsumers the logging consumers.
	 * @param samplingHandler the sampling handler.
	 */
	public static class Async {

		private final Implementation clientInfo;

		private final ClientCapabilities clientCapabilities;

		private final Map<String, Root> roots;

		private final List<Function<List<Tool>, Mono<Void>>> toolsChangeConsumers;

		private final List<Function<List<Resource>, Mono<Void>>> resourcesChangeConsumers;

		private final List<Function<List<Prompt>, Mono<Void>>> promptsChangeConsumers;

		private final List<Function<LoggingMessageNotification, Mono<Void>>> loggingConsumers;

		private final Function<CreateMessageRequest, Mono<CreateMessageResult>> samplingHandler;

		/**
		 * Create an instance and validate the arguments.
		 * @param clientCapabilities the client capabilities.
		 * @param roots the roots.
		 * @param toolsChangeConsumers the tools change consumers.
		 * @param resourcesChangeConsumers the resources change consumers.
		 * @param promptsChangeConsumers the prompts change consumers.
		 * @param loggingConsumers the logging consumers.
		 * @param samplingHandler the sampling handler.
		 */
		public Async(Implementation clientInfo, ClientCapabilities clientCapabilities, Map<String, Root> roots,
				List<Function<List<Tool>, Mono<Void>>> toolsChangeConsumers,
				List<Function<List<Resource>, Mono<Void>>> resourcesChangeConsumers,
				List<Function<List<Prompt>, Mono<Void>>> promptsChangeConsumers,
				List<Function<LoggingMessageNotification, Mono<Void>>> loggingConsumers,
				Function<CreateMessageRequest, Mono<CreateMessageResult>> samplingHandler) {

			Assert.notNull(clientInfo, "Client info must not be null");
			this.clientInfo = clientInfo;
			this.clientCapabilities = (clientCapabilities != null) ? clientCapabilities
					: new ClientCapabilities(null,
							!Utils.isEmpty(roots) ? new ClientCapabilities.RootCapabilities(false) : null,
							samplingHandler != null ? new ClientCapabilities.Sampling() : null);
			this.roots = roots != null ? new ConcurrentHashMap<>(roots) : new ConcurrentHashMap<>();

			this.toolsChangeConsumers = toolsChangeConsumers != null ? toolsChangeConsumers : List.of();
			this.resourcesChangeConsumers = resourcesChangeConsumers != null ? resourcesChangeConsumers : List.of();
			this.promptsChangeConsumers = promptsChangeConsumers != null ? promptsChangeConsumers : List.of();
			this.loggingConsumers = loggingConsumers != null ? loggingConsumers : List.of();
			this.samplingHandler = samplingHandler;
		}

		public Implementation clientInfo() {
			return this.clientInfo;
		}

		public ClientCapabilities clientCapabilities() {
			return this.clientCapabilities;
		}

		public Map<String, Root> roots() {
			return this.roots;
		}

		public List<Function<List<Tool>, Mono<Void>>> toolsChangeConsumers() {
			return this.toolsChangeConsumers;
		}

		public List<Function<List<Resource>, Mono<Void>>> resourcesChangeConsumers() {
			return this.resourcesChangeConsumers;
		}

		public List<Function<List<Prompt>, Mono<Void>>> promptsChangeConsumers() {
			return this.promptsChangeConsumers;
		}

		public List<Function<LoggingMessageNotification, Mono<Void>>> loggingConsumers() {
			return this.loggingConsumers;
		}

		public Function<CreateMessageRequest, Mono<CreateMessageResult>> samplingHandler() {
			return this.samplingHandler;
		}

		/**
		 * Convert a synchronous specification into an asynchronous one and provide
		 * blocking code offloading to prevent accidental blocking of the non-blocking
		 * transport.
		 * @param syncSpec a potentially blocking, synchronous specification.
		 * @return a specification which is protected from blocking calls specified by the
		 * user.
		 */
		public static Async fromSync(Sync syncSpec) {
			List<Function<List<Tool>, Mono<Void>>> toolsChangeConsumers = new ArrayList<>();
			for (Consumer<List<Tool>> consumer : syncSpec.toolsChangeConsumers()) {
				toolsChangeConsumers.add(t -> Mono.<Void>fromRunnable(() -> consumer.accept(t))
					.subscribeOn(Schedulers.boundedElastic()));
			}

			List<Function<List<Resource>, Mono<Void>>> resourcesChangeConsumers = new ArrayList<>();
			for (Consumer<List<Resource>> consumer : syncSpec.resourcesChangeConsumers()) {
				resourcesChangeConsumers.add(r -> Mono.<Void>fromRunnable(() -> consumer.accept(r))
					.subscribeOn(Schedulers.boundedElastic()));
			}

			List<Function<List<Prompt>, Mono<Void>>> promptsChangeConsumers = new ArrayList<>();

			for (Consumer<List<Prompt>> consumer : syncSpec.promptsChangeConsumers()) {
				promptsChangeConsumers.add(p -> Mono.<Void>fromRunnable(() -> consumer.accept(p))
					.subscribeOn(Schedulers.boundedElastic()));
			}

			List<Function<LoggingMessageNotification, Mono<Void>>> loggingConsumers = new ArrayList<>();
			for (Consumer<LoggingMessageNotification> consumer : syncSpec.loggingConsumers()) {
				loggingConsumers.add(l -> Mono.<Void>fromRunnable(() -> consumer.accept(l))
					.subscribeOn(Schedulers.boundedElastic()));
			}

			Function<CreateMessageRequest, Mono<CreateMessageResult>> samplingHandler = r -> Mono
				.fromCallable(() -> syncSpec.samplingHandler().apply(r))
				.subscribeOn(Schedulers.boundedElastic());
			return new Async(syncSpec.clientInfo(), syncSpec.clientCapabilities(), syncSpec.roots(),
					toolsChangeConsumers, resourcesChangeConsumers, promptsChangeConsumers, loggingConsumers,
					samplingHandler);
		}

	}

	/**
	 * Synchronous client features specification providing the capabilities and request
	 * and notification handlers.
	 *
	 * @param clientInfo the client implementation information.
	 * @param clientCapabilities the client capabilities.
	 * @param roots the roots.
	 * @param toolsChangeConsumers the tools change consumers.
	 * @param resourcesChangeConsumers the resources change consumers.
	 * @param promptsChangeConsumers the prompts change consumers.
	 * @param loggingConsumers the logging consumers.
	 * @param samplingHandler the sampling handler.
	 */
	public static class Sync {

		private final Implementation clientInfo;

		private final ClientCapabilities clientCapabilities;

		private final Map<String, Root> roots;

		private final List<Consumer<List<Tool>>> toolsChangeConsumers;

		private final List<Consumer<List<Resource>>> resourcesChangeConsumers;

		private final List<Consumer<List<Prompt>>> promptsChangeConsumers;

		private final List<Consumer<LoggingMessageNotification>> loggingConsumers;

		private final Function<CreateMessageRequest, CreateMessageResult> samplingHandler;

		/**
		 * Create an instance and validate the arguments.
		 * @param clientInfo the client implementation information.
		 * @param clientCapabilities the client capabilities.
		 * @param roots the roots.
		 * @param toolsChangeConsumers the tools change consumers.
		 * @param resourcesChangeConsumers the resources change consumers.
		 * @param promptsChangeConsumers the prompts change consumers.
		 * @param loggingConsumers the logging consumers.
		 * @param samplingHandler the sampling handler.
		 */
		public Sync(Implementation clientInfo, ClientCapabilities clientCapabilities, Map<String, Root> roots,
				List<Consumer<List<Tool>>> toolsChangeConsumers,
				List<Consumer<List<Resource>>> resourcesChangeConsumers,
				List<Consumer<List<Prompt>>> promptsChangeConsumers,
				List<Consumer<LoggingMessageNotification>> loggingConsumers,
				Function<CreateMessageRequest, CreateMessageResult> samplingHandler) {

			Assert.notNull(clientInfo, "Client info must not be null");
			this.clientInfo = clientInfo;
			this.clientCapabilities = (clientCapabilities != null) ? clientCapabilities
					: new ClientCapabilities(null,
							!Utils.isEmpty(roots) ? new ClientCapabilities.RootCapabilities(false) : null,
							samplingHandler != null ? new ClientCapabilities.Sampling() : null);
			this.roots = roots != null ? new HashMap<>(roots) : new HashMap<>();

			this.toolsChangeConsumers = toolsChangeConsumers != null ? toolsChangeConsumers : List.of();
			this.resourcesChangeConsumers = resourcesChangeConsumers != null ? resourcesChangeConsumers : List.of();
			this.promptsChangeConsumers = promptsChangeConsumers != null ? promptsChangeConsumers : List.of();
			this.loggingConsumers = loggingConsumers != null ? loggingConsumers : List.of();
			this.samplingHandler = samplingHandler;
		}

		public Implementation clientInfo() {
			return this.clientInfo;
		}

		public ClientCapabilities clientCapabilities() {
			return this.clientCapabilities;
		}

		public Map<String, Root> roots() {
			return this.roots;
		}

		public List<Consumer<List<Tool>>> toolsChangeConsumers() {
			return this.toolsChangeConsumers;
		}

		public List<Consumer<List<Resource>>> resourcesChangeConsumers() {
			return this.resourcesChangeConsumers;
		}

		public List<Consumer<List<Prompt>>> promptsChangeConsumers() {
			return this.promptsChangeConsumers;
		}

		public List<Consumer<LoggingMessageNotification>> loggingConsumers() {
			return this.loggingConsumers;
		}

		public Function<CreateMessageRequest, CreateMessageResult> samplingHandler() {
			return this.samplingHandler;
		}

	}

}
