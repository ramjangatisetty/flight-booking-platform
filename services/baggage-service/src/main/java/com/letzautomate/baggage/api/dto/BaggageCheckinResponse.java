package com.letzautomate.baggage.api.dto;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import java.time.Instant;

@JacksonXmlRootElement(localName = "BaggageCheckinResponse", namespace = "http://letzautomate.com/baggage/v1")
public class BaggageCheckinResponse {

	@JacksonXmlProperty(localName = "bagTag", namespace = "http://letzautomate.com/baggage/v1")
	private String bagTag;

	@JacksonXmlProperty(localName = "status", namespace = "http://letzautomate.com/baggage/v1")
	private String status;

	@JacksonXmlProperty(localName = "acceptedAt", namespace = "http://letzautomate.com/baggage/v1")
	private Instant acceptedAt;

	public BaggageCheckinResponse() {}

	public BaggageCheckinResponse(String bagTag, String status, Instant acceptedAt) {
		this.bagTag = bagTag;
		this.status = status;
		this.acceptedAt = acceptedAt;
	}

	public String getBagTag() {
		return bagTag;
	}

	public void setBagTag(String bagTag) {
		this.bagTag = bagTag;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public Instant getAcceptedAt() {
		return acceptedAt;
	}

	public void setAcceptedAt(Instant acceptedAt) {
		this.acceptedAt = acceptedAt;
	}
}
