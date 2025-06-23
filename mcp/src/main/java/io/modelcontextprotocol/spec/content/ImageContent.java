package io.modelcontextprotocol.spec.content;

import java.util.List;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.modelcontextprotocol.spec.common.Role;

/**
 * 图片内容。
 *
 * @param audience 目标受众
 * @param priority 优先级
 * @param data 数据
 * @param mimeType 媒体类型
 */
public final class ImageContent implements Content {

	private final List<Role> audience;

	private final Double priority;

	private final String data;

	private final String mimeType;

	@JsonCreator
	public ImageContent(@JsonProperty("audience") List<Role> audience, @JsonProperty("priority") Double priority,
			@JsonProperty("data") String data, @JsonProperty("mimeType") String mimeType) {
		this.audience = audience;
		this.priority = priority;
		this.data = data;
		this.mimeType = mimeType;
	}

	@Override
	public String getType() {
		return "image";
	}

	public List<Role> getAudience() {
		return this.audience;
	}

	public Double getPriority() {
		return this.priority;
	}

	public String getData() {
		return this.data;
	}

	public String getMimeType() {
		return this.mimeType;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null || getClass() != obj.getClass()) {
			return false;
		}
		ImageContent that = (ImageContent) obj;
		return Objects.equals(this.audience, that.audience) && Objects.equals(this.priority, that.priority)
				&& Objects.equals(this.data, that.data) && Objects.equals(this.mimeType, that.mimeType);
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.audience, this.priority, this.data, this.mimeType);
	}

	@Override
	public String toString() {
		return "ImageContent[" + "audience=" + this.audience + ", " + "priority=" + this.priority + ", " + "data="
				+ this.data + ", " + "mimeType=" + this.mimeType + ']';
	}

}
