package io.modelcontextprotocol.spec.jsonrpc;

/**
 * MCP JSON-RPC响应中使用的标准错误码。
 */
public final class ErrorCodes {

	/** 无效JSON。 */
	public static final int PARSE_ERROR = -32700;

	/** 非法请求对象。 */
	public static final int INVALID_REQUEST = -32600;

	/** 方法不存在或不可用。 */
	public static final int METHOD_NOT_FOUND = -32601;

	/** 方法参数无效。 */
	public static final int INVALID_PARAMS = -32602;

	/** 内部错误。 */
	public static final int INTERNAL_ERROR = -32603;

	private ErrorCodes() {
	}

}
