package com.letzautomate.baggage.api.dto;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

import java.time.Instant;

public class BaggageEvent {

	@JacksonXmlProperty(localName = "eventType", namespace = "http://letzautomate.com/baggage/v1")
	private String eventType;

	@JacksonXmlProperty(localName = "airport", namespace = "http://letzautomate.com/baggage/v1")
	private String airport;

	@JacksonXmlProperty(localName = "occurredAt", namespace = "http://letzautomate.com/baggage/v1")
	private Instant occurredAt;

	public BaggageEvent() {}

	public BaggageEvent(String eventType, String airport, Instant occurredAt) {
		this.eventType = eventType;
		this.airport = airport;
		this.occurredAt = occurredAt;
	}

	public String getEventType() {
		return eventType;
	}

	public void setEventType(String eventType) {
		this.eventType = eventType;
	}

	public String getAirport() {
		return airport;
	}

	public void setAirport(String airport) {
		this.airport = airport;
	}

	public Instant getOccurredAt() {
		return occurredAt;
	}

	public void setOccurredAt(Instant occurredAt) {
		this.occurredAt = occurredAt;
	}
}
