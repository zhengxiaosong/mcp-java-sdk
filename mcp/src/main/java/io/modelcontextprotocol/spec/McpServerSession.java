package io.modelcontextprotocol.spec;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

import com.fasterxml.jackson.core.type.TypeReference;
import io.modelcontextprotocol.server.McpAsyncServerExchange;
import io.modelcontextprotocol.spec.initialization.ClientCapabilities;
import io.modelcontextprotocol.spec.initialization.Implementation;
import io.modelcontextprotocol.spec.initialization.InitializeRequest;
import io.modelcontextprotocol.spec.initialization.InitializeResult;
import io.modelcontextprotocol.spec.jsonrpc.ErrorCodes;
import io.modelcontextprotocol.spec.jsonrpc.JSONRPCMessage;
import io.modelcontextprotocol.spec.jsonrpc.JSONRPCNotification;
import io.modelcontextprotocol.spec.jsonrpc.JSONRPCRequest;
import io.modelcontextprotocol.spec.jsonrpc.JSONRPCResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;
import reactor.core.publisher.MonoSink;
import reactor.core.publisher.Sinks;

/**
 * Represents a Model Control Protocol (MCP) session on the server side. It manages
 * bidirectional JSON-RPC communication with the client.
 */
public class McpServerSession implements McpSession {

	private static final Logger logger = LoggerFactory.getLogger(McpServerSession.class);

	private final ConcurrentHashMap<Object, MonoSink<JSONRPCResponse>> pendingResponses = new ConcurrentHashMap<>();

	private final String id;

	/** Duration to wait for request responses before timing out */
	private final Duration requestTimeout;

	private final AtomicLong requestCounter = new AtomicLong(0);

	private final InitRequestHandler initRequestHandler;

	private final InitNotificationHandler initNotificationHandler;

	private final Map<String, RequestHandler<?>> requestHandlers;

	private final Map<String, NotificationHandler> notificationHandlers;

	private final McpServerTransport transport;

	private final Sinks.One<McpAsyncServerExchange> exchangeSink = Sinks.one();

	private final AtomicReference<ClientCapabilities> clientCapabilities = new AtomicReference<>();

	private final AtomicReference<Implementation> clientInfo = new AtomicReference<>();

	private static final int STATE_UNINITIALIZED = 0;

	private static final int STATE_INITIALIZING = 1;

	private static final int STATE_INITIALIZED = 2;

	private final AtomicInteger state = new AtomicInteger(STATE_UNINITIALIZED);

	/**
	 * Creates a new server session with the given parameters and the transport to use.
	 * @param id session id
	 * @param transport the transport to use
	 * @param initHandler called when a
	 * {@link io.modelcontextprotocol.spec.InitializeRequest} is received by the server
	 * @param initNotificationHandler called when a
	 * {@link io.modelcontextprotocol.spec.McpSchema#METHOD_NOTIFICATION_INITIALIZED} is
	 * received.
	 * @param requestHandlers map of request handlers to use
	 * @param notificationHandlers map of notification handlers to use
	 */
	public McpServerSession(String id, Duration requestTimeout, McpServerTransport transport,
			InitRequestHandler initHandler, InitNotificationHandler initNotificationHandler,
			Map<String, RequestHandler<?>> requestHandlers, Map<String, NotificationHandler> notificationHandlers) {
		this.id = id;
		this.requestTimeout = requestTimeout;
		this.transport = transport;
		this.initRequestHandler = initHandler;
		this.initNotificationHandler = initNotificationHandler;
		this.requestHandlers = requestHandlers;
		this.notificationHandlers = notificationHandlers;
	}

	/**
	 * Retrieve the session id.
	 * @return session id
	 */
	public String getId() {
		return this.id;
	}

	/**
	 * Called upon successful initialization sequence between the client and the server
	 * with the client capabilities and information.
	 *
	 * <a href=
	 * "https://github.com/modelcontextprotocol/specification/blob/main/docs/specification/basic/lifecycle.md#initialization">Initialization
	 * Spec</a>
	 * @param clientCapabilities the capabilities the connected client provides
	 * @param clientInfo the information about the connected client
	 */
	public void init(ClientCapabilities clientCapabilities, Implementation clientInfo) {
		this.clientCapabilities.lazySet(clientCapabilities);
		this.clientInfo.lazySet(clientInfo);
	}

	private String generateRequestId() {
		return this.id + "-" + this.requestCounter.getAndIncrement();
	}

	@Override
	public <T> Mono<T> sendRequest(String method, Object requestParams, TypeReference<T> typeRef) {
		String requestId = this.generateRequestId();

		return Mono.<JSONRPCResponse>create(sink -> {
			this.pendingResponses.put(requestId, sink);
			JSONRPCRequest jsonrpcRequest = new JSONRPCRequest(McpSchema.JSONRPC_VERSION, method, requestId,
					requestParams);
			this.transport.sendMessage(jsonrpcRequest).subscribe(v -> {
			}, error -> {
				this.pendingResponses.remove(requestId);
				sink.error(error);
			});
		}).timeout(requestTimeout).handle((jsonRpcResponse, sink) -> {
			if (jsonRpcResponse.getError() != null) {
				sink.error(new McpError(jsonRpcResponse.getError()));
			}
			else {
				if (typeRef.getType().equals(Void.class)) {
					sink.complete();
				}
				else {
					sink.next(this.transport.unmarshalFrom(jsonRpcResponse.getResult(), typeRef));
				}
			}
		});
	}

	@Override
	public Mono<Void> sendNotification(String method, Object params) {
		JSONRPCNotification jsonrpcNotification = new JSONRPCNotification(McpSchema.JSONRPC_VERSION, method, params);
		return this.transport.sendMessage(jsonrpcNotification);
	}

	/**
	 * Called by the {@link McpServerTransportProvider} once the session is determined.
	 * The purpose of this method is to dispatch the message to an appropriate handler as
	 * specified by the MCP server implementation
	 * ({@link io.modelcontextprotocol.server.McpAsyncServer} or
	 * {@link io.modelcontextprotocol.server.McpSyncServer}) via
	 * {@link McpServerSession.Factory} that the server creates.
	 * @param message the incoming JSON-RPC message
	 * @return a Mono that completes when the message is processed
	 */
	public Mono<Void> handle(JSONRPCMessage message) {
		return Mono.defer(() -> {
			// TODO handle errors for communication to without initialization happening
			// first
			if (message instanceof JSONRPCResponse) {
				JSONRPCResponse response = (JSONRPCResponse) message;
				logger.debug("Received Response: {}", response);
				var sink = pendingResponses.remove(response.getId());
				if (sink == null) {
					logger.warn("Unexpected response for unknown id {}", response.getId());
				}
				else {
					sink.success(response);
				}
				return Mono.empty();
			}
			else if (message instanceof JSONRPCRequest) {
				JSONRPCRequest request = (JSONRPCRequest) message;
				logger.debug("Received request: {}", request);
				return handleIncomingRequest(request).onErrorResume(error -> {
					var errorResponse = new JSONRPCResponse(McpSchema.JSONRPC_VERSION, request.getId(), null,
							new JSONRPCResponse.JSONRPCError(ErrorCodes.INTERNAL_ERROR, error.getMessage(), null));
					// TODO: Should the error go to SSE or back as POST return?
					return this.transport.sendMessage(errorResponse).then(Mono.empty());
				}).flatMap(this.transport::sendMessage);
			}
			else if (message instanceof JSONRPCNotification) {
				JSONRPCNotification notification = (JSONRPCNotification) message;
				// TODO handle errors for communication to without initialization
				// happening first
				logger.debug("Received notification: {}", notification);
				// TODO: in case of error, should the POST request be signalled?
				return handleIncomingNotification(notification)
					.doOnError(error -> logger.error("Error handling notification: {}", error.getMessage()));
			}
			else {
				logger.warn("Received unknown message type: {}", message);
				return Mono.empty();
			}
		});
	}

	/**
	 * Handles an incoming JSON-RPC request by routing it to the appropriate handler.
	 * @param request The incoming JSON-RPC request
	 * @return A Mono containing the JSON-RPC response
	 */
	private Mono<JSONRPCResponse> handleIncomingRequest(JSONRPCRequest request) {
		return Mono.defer(() -> {
			Mono<?> resultMono;
			if (McpSchema.METHOD_INITIALIZE.equals(request.getMethod())) {
				// TODO handle situation where already initialized!
				InitializeRequest initializeRequest = transport.unmarshalFrom(request.getParams(),
						new TypeReference<InitializeRequest>() {
						});

				this.state.lazySet(STATE_INITIALIZING);
				this.init(initializeRequest.getCapabilities(), initializeRequest.getClientInfo());
				resultMono = this.initRequestHandler.handle(initializeRequest);
			}
			else {
				// TODO handle errors for communication to this session without
				// initialization happening first
				var handler = this.requestHandlers.get(request.getMethod());
				if (handler == null) {
					MethodNotFoundError error = getMethodNotFoundError(request.getMethod());
					return Mono.just(new JSONRPCResponse(McpSchema.JSONRPC_VERSION, request.getId(), null,
							new JSONRPCResponse.JSONRPCError(ErrorCodes.METHOD_NOT_FOUND, error.message(),
									error.data())));
				}

				resultMono = this.exchangeSink.asMono()
					.flatMap(exchange -> handler.handle(exchange, request.getParams()));
			}
			return resultMono
				.map(result -> new JSONRPCResponse(McpSchema.JSONRPC_VERSION, request.getId(), result, null))
				.onErrorResume(error -> Mono.just(new JSONRPCResponse(McpSchema.JSONRPC_VERSION, request.getId(), null,
						new JSONRPCResponse.JSONRPCError(ErrorCodes.INTERNAL_ERROR, error.getMessage(), null)))); // TODO:
																													// add
																													// error
																													// message
																													// through
																													// the
																													// data
																													// field
		});
	}

	/**
	 * Handles an incoming JSON-RPC notification by routing it to the appropriate handler.
	 * @param notification The incoming JSON-RPC notification
	 * @return A Mono that completes when the notification is processed
	 */
	private Mono<Void> handleIncomingNotification(JSONRPCNotification notification) {
		return Mono.defer(() -> {
			if (McpSchema.METHOD_NOTIFICATION_INITIALIZED.equals(notification.getMethod())) {
				this.state.lazySet(STATE_INITIALIZED);
				exchangeSink.tryEmitValue(new McpAsyncServerExchange(this, clientCapabilities.get(), clientInfo.get()));
				return this.initNotificationHandler.handle();
			}

			var handler = notificationHandlers.get(notification.getMethod());
			if (handler == null) {
				logger.error("No handler registered for notification method: {}", notification.getMethod());
				return Mono.empty();
			}
			return this.exchangeSink.asMono().flatMap(exchange -> handler.handle(exchange, notification.getParams()));
		});
	}

	private static class MethodNotFoundError {

		private final String method;

		private final String message;

		private final Object data;

		MethodNotFoundError(String method, String message, Object data) {
			this.method = method;
			this.message = message;
			this.data = data;
		}

		@SuppressWarnings("unused")
		public String method() {
			return method;
		}

		public String message() {
			return message;
		}

		public Object data() {
			return data;
		}

	}

	private MethodNotFoundError getMethodNotFoundError(String method) {
		return new MethodNotFoundError(method, "Method not found: " + method, null);
	}

	@Override
	public Mono<Void> closeGracefully() {
		return this.transport.closeGracefully();
	}

	@Override
	public void close() {
		this.transport.close();
	}

	/**
	 * Request handler for the initialization request.
	 */
	public interface InitRequestHandler {

		/**
		 * Handles the initialization request.
		 * @param initializeRequest the initialization request by the client
		 * @return a Mono that will emit the result of the initialization
		 */
		Mono<InitializeResult> handle(InitializeRequest initializeRequest);

	}

	/**
	 * Notification handler for the initialization notification from the client.
	 */
	public interface InitNotificationHandler {

		/**
		 * Specifies an action to take upon successful initialization.
		 * @return a Mono that will complete when the initialization is acted upon.
		 */
		Mono<Void> handle();

	}

	/**
	 * A handler for client-initiated notifications.
	 */
	public interface NotificationHandler {

		/**
		 * Handles a notification from the client.
		 * @param exchange the exchange associated with the client that allows calling
		 * back to the connected client or inspecting its capabilities.
		 * @param params the parameters of the notification.
		 * @return a Mono that completes once the notification is handled.
		 */
		Mono<Void> handle(McpAsyncServerExchange exchange, Object params);

	}

	/**
	 * A handler for client-initiated requests.
	 *
	 * @param <T> the type of the response that is expected as a result of handling the
	 * request.
	 */
	public interface RequestHandler<T> {

		/**
		 * Handles a request from the client.
		 * @param exchange the exchange associated with the client that allows calling
		 * back to the connected client or inspecting its capabilities.
		 * @param params the parameters of the request.
		 * @return a Mono that will emit the response to the request.
		 */
		Mono<T> handle(McpAsyncServerExchange exchange, Object params);

	}

	/**
	 * Factory for creating server sessions which delegate to a provided 1:1 transport
	 * with a connected client.
	 */
	@FunctionalInterface
	public interface Factory {

		/**
		 * Creates a new 1:1 representation of the client-server interaction.
		 * @param sessionTransport the transport to use for communication with the client.
		 * @return a new server session.
		 */
		McpServerSession create(McpServerTransport sessionTransport);

	}

}
