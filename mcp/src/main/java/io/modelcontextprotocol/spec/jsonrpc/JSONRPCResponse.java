package io.modelcontextprotocol.spec.jsonrpc;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;

/**
 * JSON-RPC响应消息。
 */
@JsonInclude(JsonInclude.Include.NON_ABSENT)
@JsonIgnoreProperties(ignoreUnknown = true)
public class JSONRPCResponse implements JSONRPCMessage {

	@JsonProperty("jsonrpc")
	private final String jsonrpc;

	@JsonProperty("id")
	private final Object id;

	@JsonProperty("result")
	private final Object result;

	@JsonProperty("error")
	private final JSONRPCError error;

	public JSONRPCResponse(@JsonProperty("jsonrpc") String jsonrpc, @JsonProperty("id") Object id,
			@JsonProperty("result") Object result, @JsonProperty("error") JSONRPCError error) {
		this.jsonrpc = jsonrpc;
		this.id = id;
		this.result = result;
		this.error = error;
	}

	@Override
	public String jsonrpc() {
		return jsonrpc;
	}

	public Object getId() {
		return id;
	}

	public Object getResult() {
		return result;
	}

	public JSONRPCError getError() {
		return error;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		JSONRPCResponse that = (JSONRPCResponse) o;
		return Objects.equals(jsonrpc, that.jsonrpc) && Objects.equals(id, that.id)
				&& Objects.equals(result, that.result) && Objects.equals(error, that.error);
	}

	@Override
	public int hashCode() {
		return Objects.hash(jsonrpc, id, result, error);
	}

	@Override
	public String toString() {
		return "JSONRPCResponse{" + "jsonrpc='" + jsonrpc + '\'' + ", id=" + id + ", result=" + result + ", error="
				+ error + '}';
	}

	@JsonInclude(JsonInclude.Include.NON_ABSENT)
	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class JSONRPCError {

		@JsonProperty("code")
		private final int code;

		@JsonProperty("message")
		private final String message;

		@JsonProperty("data")
		private final Object data;

		public JSONRPCError(@JsonProperty("code") int code, @JsonProperty("message") String message,
				@JsonProperty("data") Object data) {
			this.code = code;
			this.message = message;
			this.data = data;
		}

		public int getCode() {
			return code;
		}

		public String getMessage() {
			return message;
		}

		public Object getData() {
			return data;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o)
				return true;
			if (o == null || getClass() != o.getClass())
				return false;
			JSONRPCError that = (JSONRPCError) o;
			return code == that.code && Objects.equals(message, that.message) && Objects.equals(data, that.data);
		}

		@Override
		public int hashCode() {
			return Objects.hash(code, message, data);
		}

		@Override
		public String toString() {
			return "JSONRPCError{" + "code=" + code + ", message='" + message + '\'' + ", data=" + data + '}';
		}

	}

}
