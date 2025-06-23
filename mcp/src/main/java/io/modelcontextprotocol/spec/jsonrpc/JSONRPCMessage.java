package io.modelcontextprotocol.spec.jsonrpc;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * JSON-RPC消息类型接口。
 */
/**
 * JSON-RPC消息类型接口。
 */
@JsonTypeInfo(
    use = JsonTypeInfo.Id.DEDUCTION,
    include = JsonTypeInfo.As.PROPERTY
)
@JsonSubTypes({
    @JsonSubTypes.Type(JSONRPCRequest.class),
    @JsonSubTypes.Type(JSONRPCNotification.class),
    @JsonSubTypes.Type(JSONRPCResponse.class)
})
public interface JSONRPCMessage {

	String jsonrpc();

}
