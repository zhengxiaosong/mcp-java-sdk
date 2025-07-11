package io.modelcontextprotocol.spec.jsonrpc;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;

/**
 * JSON-RPC通知消息。
 */
@JsonInclude(JsonInclude.Include.NON_ABSENT)
@JsonIgnoreProperties(ignoreUnknown = true)
public class JSONRPCNotification implements JSONRPCMessage {

	@JsonProperty("jsonrpc")
	private final String jsonrpc;

	@JsonProperty("method")
	private final String method;

	@JsonProperty("params")
	private final Object params;

	public JSONRPCNotification(@JsonProperty("jsonrpc") String jsonrpc, @JsonProperty("method") String method,
			@JsonProperty("params") Object params) {
		this.jsonrpc = jsonrpc;
		this.method = method;
		this.params = params;
	}

	@Override
	public String jsonrpc() {
		return jsonrpc;
	}

	public String getMethod() {
		return method;
	}

	public Object getParams() {
		return params;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		JSONRPCNotification that = (JSONRPCNotification) o;
		return Objects.equals(jsonrpc, that.jsonrpc) && Objects.equals(method, that.method)
				&& Objects.equals(params, that.params);
	}

	@Override
	public int hashCode() {
		return Objects.hash(jsonrpc, method, params);
	}

	@Override
	public String toString() {
		return "JSONRPCNotification{" + "jsonrpc='" + jsonrpc + '\'' + ", method='" + method + '\'' + ", params="
				+ params + '}';
	}

}
