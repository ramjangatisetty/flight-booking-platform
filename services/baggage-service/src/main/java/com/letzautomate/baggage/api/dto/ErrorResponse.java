package com.letzautomate.baggage.api.dto;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

@JacksonXmlRootElement(localName = "Error", namespace = "http://letzautomate.com/baggage/v1")
public class ErrorResponse {

	@JacksonXmlProperty(localName = "code", namespace = "http://letzautomate.com/baggage/v1")
	private String code;

	@JacksonXmlProperty(localName = "message", namespace = "http://letzautomate.com/baggage/v1")
	private String message;

	@JacksonXmlProperty(localName = "bagTag", namespace = "http://letzautomate.com/baggage/v1")
	private String bagTag;

	public ErrorResponse() {}

	public ErrorResponse(String code, String message) {
		this.code = code;
		this.message = message;
	}

	public ErrorResponse(String code, String message, String bagTag) {
		this.code = code;
		this.message = message;
		this.bagTag = bagTag;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getBagTag() {
		return bagTag;
	}

	public void setBagTag(String bagTag) {
		this.bagTag = bagTag;
	}
}
