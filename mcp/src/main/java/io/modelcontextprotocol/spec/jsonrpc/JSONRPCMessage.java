package io.modelcontextprotocol.spec.jsonrpc;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * JSON-RPC消息类型接口。
 */
/**
 * JSON-RPC消息类型接口。
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({ @JsonSubTypes.Type(value = JSONRPCRequest.class, name = "request"),
		@JsonSubTypes.Type(value = JSONRPCNotification.class, name = "notification"),
		@JsonSubTypes.Type(value = JSONRPCResponse.class, name = "response") })
public interface JSONRPCMessage {

	String jsonrpc();

}
