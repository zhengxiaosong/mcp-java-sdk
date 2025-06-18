# Spring Framework Downgrade Guide

## 1. Overview

The main goal of this downgrade is to make the project compatible with Spring Boot 2.7.6. To achieve this, we need to downgrade Spring Framework from version 6.x to version 5.3.24.

## 2. Version Changes

### 2.1 Project Version

- From: `0.10.1-SNAPSHOT`
- To: `0.10.1-sp276-SNAPSHOT`

### 2.2 Major Dependency Updates

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

## 3. Major Changes

### 3.1 API Changes

1. RestClient Replacement
   - Replace `RestClient` in Spring 6.x with appropriate alternatives
   - WebFlux module: Use `WebClient`
   - WebMVC module: Use `RestTemplate`

2. Code Examples

```java
// Original Code (Spring 6.x)
String response = RestClient.create()
    .get()
    .uri(url)
    .retrieve()
    .body(String.class);

// WebFlux Module New Code (Spring 5.3.24)
String response = WebClient.create()
    .get()
    .uri(url)
    .retrieve()
    .bodyToMono(String.class)
    .block();

// WebMVC Module New Code (Spring 5.3.24)
String response = new RestTemplate()
    .getForObject(url, String.class);
```

### 3.2 Package Name Changes

- Migrated from `jakarta.servlet` related dependencies to `javax.servlet`
- Updated related import statements
- Main affected classes:
  - `HttpServletRequest`
  - `HttpServletResponse`
  - `ServletContext`
  - Other Servlet related classes

### 3.3 Test Code Adaptation

1. WebFlux Tests (`WebFluxSseIntegrationTests`)

    ```java
    // Before
    import org.springframework.web.client.RestClient;
    // ...
    String response = RestClient.create()
        .get()
        .uri(url)
        .retrieve()
        .body(String.class);

    // After
    import org.springframework.web.reactive.function.client.WebClient;
    // ...
    String response = WebClient.create()
        .get()
        .uri(url)
        .retrieve()
        .bodyToMono(String.class)
        .block();
    ```

2. WebMVC Tests (`WebMvcSseIntegrationTests`)

    ```java
    // Before
    import org.springframework.web.client.RestClient;
    // ...
    String response = RestClient.create()
        .get()
        .uri(url)
        .retrieve()
        .body(String.class);

    // After
    import org.springframework.web.client.RestTemplate;
    // ...
    String response = new RestTemplate()
        .getForObject(url, String.class);
    ```

## 4. Compatibility Notes

### 4.1 Functional Compatibility

- All core functionalities remain unchanged
- API calling methods have changed, but functionality remains consistent
- Test coverage maintains the original level
- Ensures full compatibility with Spring Boot 2.7.6

### 4.2 Important Notes

1. HTTP Client Usage
   - WebFlux module uses `WebClient` for reactive programming
   - WebMVC module uses `RestTemplate` for synchronous calls
   - Pay attention to blocking operation scenarios

2. Dependency Management
   - Ensure all Spring-related dependency versions are consistent
   - Pay attention to transitive dependency version compatibility
   - Use Maven BOM for version management

3. Potential Risks
   - Differences in reactive programming APIs
   - Performance impact of blocking operations
   - Third-party library compatibility issues

## 5. Summary

This downgrade work primarily focused on adapting to Spring Framework 5.3.24, successfully achieving compatibility with Spring Boot 2.7.6. Through appropriate API replacements and code adjustments, we ensured the normal operation of system functions. It is recommended to focus on performance optimization and test coverage in the future to ensure system stability and maintainability.