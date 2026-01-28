package com.letzautomate.baggage.api.dto;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import java.time.Instant;

@JacksonXmlRootElement(localName = "BaggageStatusUpdateResponse")
public class BaggageStatusUpdateResponse {

	@JacksonXmlProperty(localName = "bagTag")
	private String bagTag;

	@JacksonXmlProperty(localName = "status")
	private String status;

	@JacksonXmlProperty(localName = "updatedAt")
	private Instant updatedAt;

	public BaggageStatusUpdateResponse() {
	}

	public BaggageStatusUpdateResponse(String bagTag, String status, Instant updatedAt) {
		this.bagTag = bagTag;
		this.status = status;
		this.updatedAt = updatedAt;
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

	public Instant getUpdatedAt() {
		return updatedAt;
	}

	public void setUpdatedAt(Instant updatedAt) {
		this.updatedAt = updatedAt;
	}
}
