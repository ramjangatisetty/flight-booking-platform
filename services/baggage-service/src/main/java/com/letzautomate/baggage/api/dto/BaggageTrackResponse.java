package com.letzautomate.baggage.api.dto;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import java.util.List;

@JacksonXmlRootElement(localName = "BaggageTrackResponse", namespace = "http://letzautomate.com/baggage/v1")
public class BaggageTrackResponse {

	@JacksonXmlProperty(localName = "bagTag", namespace = "http://letzautomate.com/baggage/v1")
	private String bagTag;

	@JacksonXmlProperty(localName = "status", namespace = "http://letzautomate.com/baggage/v1")
	private String status;

	@JacksonXmlProperty(localName = "events", namespace = "http://letzautomate.com/baggage/v1")
	@JacksonXmlElementWrapper(useWrapping = false)
	private List<BaggageEvent> events;

	public BaggageTrackResponse() {}

	public BaggageTrackResponse(String bagTag, String status, List<BaggageEvent> events) {
		this.bagTag = bagTag;
		this.status = status;
		this.events = events;
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

	public List<BaggageEvent> getEvents() {
		return events;
	}

	public void setEvents(List<BaggageEvent> events) {
		this.events = events;
	}
}
