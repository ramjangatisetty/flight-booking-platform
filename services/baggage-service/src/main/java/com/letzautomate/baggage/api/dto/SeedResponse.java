package com.letzautomate.baggage.api.dto;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

@JacksonXmlRootElement(localName = "SeedResponse", namespace = "http://letzautomate.com/baggage/v1")
public class SeedResponse {

	@JacksonXmlProperty(localName = "bagTag", namespace = "http://letzautomate.com/baggage/v1")
	private String bagTag;

	@JacksonXmlProperty(localName = "message", namespace = "http://letzautomate.com/baggage/v1")
	private String message;

	public SeedResponse() {}

	public SeedResponse(String bagTag, String message) {
		this.bagTag = bagTag;
		this.message = message;
	}

	public String getBagTag() {
		return bagTag;
	}

	public void setBagTag(String bagTag) {
		this.bagTag = bagTag;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}
}
