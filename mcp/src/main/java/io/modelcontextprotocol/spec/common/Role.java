package io.modelcontextprotocol.spec.common;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 用户和助手角色枚举。
 */
public enum Role {

	@JsonProperty("user")
	USER, @JsonProperty("assistant")
	ASSISTANT

}
