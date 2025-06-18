# Spring Framework 降级说明

## 1. 概述

本次降级的主要目标是将项目与Spring Boot 2.7.6兼容。为此，我们需要将Spring Framework从6.x版本降级到5.3.24版本。

## 2. 版本变更

### 2.1 项目版本

- 从: `0.10.1-SNAPSHOT`
- 到: `0.10.1-sp276-SNAPSHOT`

### 2.2 主要依赖版本更新

```xml
<properties>
    <springframework.version>5.3.24</springframework.version>
    <slf4j-api.version>1.7.36</slf4j-api.version>
    <logback.version>1.2.12</logback.version>
    <jackson.version>2.13.5</jackson.version>
    <reactor.version>3.4.24</reactor.version>
    <reactor-netty.version>1.0.24</reactor-netty.version>
    <javax.servlet-api.version>4.0.1</javax.servlet-api.version>
</properties>
```

## 3. 主要改动

### 3.1 API变更

1. RestClient替换
   - 将Spring 6.x中的`RestClient`替换为适当的替代方案
   - WebFlux模块：使用`WebClient`
   - WebMVC模块：使用`RestTemplate`

2. 代码示例

```java
// 原代码 (Spring 6.x)
String response = RestClient.create()
    .get()
    .uri(url)
    .retrieve()
    .body(String.class);

// WebFlux模块新代码 (Spring 5.3.24)
String response = WebClient.create()
    .get()
    .uri(url)
    .retrieve()
    .bodyToMono(String.class)
    .block();

// WebMVC模块新代码 (Spring 5.3.24)
String response = new RestTemplate()
    .getForObject(url, String.class);
```

### 3.2 包名变更

- 从`jakarta.servlet`相关依赖迁移到`javax.servlet`
- 更新了相关的导入语句
- 主要影响的类：
  - `HttpServletRequest`
  - `HttpServletResponse`
  - `ServletContext`
  - 其他Servlet相关的类

### 3.3 测试代码适配

1. WebFlux测试（`WebFluxSseIntegrationTests`）

    ```java
    // 修改前
    import org.springframework.web.client.RestClient;
    // ...
    String response = RestClient.create()
        .get()
        .uri(url)
        .retrieve()
        .body(String.class);

    // 修改后
    import org.springframework.web.reactive.function.client.WebClient;
    // ...
    String response = WebClient.create()
        .get()
        .uri(url)
        .retrieve()
        .bodyToMono(String.class)
        .block();
    ```

2. WebMVC测试（`WebMvcSseIntegrationTests`）

    ```java
    // 修改前
    import org.springframework.web.client.RestClient;
    // ...
    String response = RestClient.create()
        .get()
        .uri(url)
        .retrieve()
        .body(String.class);

    // 修改后
    import org.springframework.web.client.RestTemplate;
    // ...
    String response = new RestTemplate()
        .getForObject(url, String.class);
    ```

## 4. 兼容性说明

### 4.1 功能兼容性

- 所有核心功能保持不变
- API调用方式有所改变，但功能保持一致
- 测试覆盖率维持原有水平
- 确保了与Spring Boot 2.7.6的完全兼容性

### 4.2 注意事项

1. HTTP客户端使用
   - WebFlux模块使用`WebClient`进行响应式编程
   - WebMVC模块使用`RestTemplate`进行同步调用
   - 需要注意阻塞操作的使用场景

2. 依赖管理
   - 确保所有Spring相关依赖版本一致
   - 注意传递依赖的版本兼容性
   - 使用Maven BOM来管理版本

3. 潜在风险
   - 响应式编程API的差异
   - 阻塞操作的性能影响
   - 第三方库的兼容性问题

## 5. 总结

本次降级工作主要围绕Spring Framework 5.3.24的适配进行，成功实现了与Spring Boot 2.7.6的兼容。通过合理的API替换和代码调整，保证了系统功能的正常运行。建议后续关注性能优化和测试覆盖，确保系统的稳定性和可维护性。
