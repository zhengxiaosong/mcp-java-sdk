# Java 11 兼容性修复任务清单

以下是为兼容 Java 11而已修复的文件列表，用于备忘。

* [x] `mcp/src/main/java/io/modelcontextprotocol/client/transport/FlowSseClient.java`
    * **问题**: `record` 类型 (Java 16+).
    * **解决方案**: 将 `SseEvent` record 替换为兼容 Java 11 的 class.
* [x] `mcp/src/main/java/io/modelcontextprotocol/client/McpClientFeatures.java`
    * **问题**: `record` 类型 (Java 16+).
    * **解决方案**: 将 `Async` 和 `Sync` record 替换为兼容 Java 11 的 class.
* [x] `mcp/src/main/java/io/modelcontextprotocol/server/McpServerFeatures.java`
    * **问题**: 8 个嵌套的 `record` 类型 (Java 16+).
    * **解决方案**: 将所有 record 替换为兼容 Java 11 的 class.
* [x] `mcp/src/main/java/io/modelcontextprotocol/spec/McpClientSession.java`
    * **问题**: `instanceof` 模式匹配 (Java 16+) 和 `record` 类型 (Java 16+).
    * **解决方案**: 改写 `instanceof` 并将 `MethodNotFoundError` record 替换为 class.
* [x] `mcp/src/main/java/io/modelcontextprotocol/spec/McpServerSession.java`
    * **问题**: `instanceof` 模式匹配 (Java 16+) 和 `record` 类型 (Java 16+).
    * **解决方案**: 改写 `instanceof` 并将 `MethodNotFoundError` record 替换为 class.
* [x] `mcp/src/test/java/io/modelcontextprotocol/client/McpAsyncClientResponseHandlerTests.java`
    * **问题**: `instanceof` 模式匹配与条件合并 (Java 16+).
    * **解决方案**: 拆分为类型检查和内部强制类型转换的两步判断。

