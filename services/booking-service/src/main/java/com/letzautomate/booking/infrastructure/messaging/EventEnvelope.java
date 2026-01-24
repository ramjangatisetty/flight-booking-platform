package com.letzautomate.booking.infrastructure.messaging;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.time.Instant;
import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
public class EventEnvelope<T> {
	public Meta meta;
	public T data;

	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class Meta {

		// inventory-service sends: eventName
		// booking-service originally sends: eventType
		@JsonAlias({"eventName", "eventType"})
		public String eventType;

		// inventory-service sends: version
		// booking-service originally sends: eventVersion
		@JsonAlias({"version", "eventVersion"})
		public int eventVersion;

		// inventory-service sends ISO string -> Instant parses fine
		public Instant occurredAt;

		// inventory-service sends UUID as string -> UUID parses fine
		public UUID correlationId;

		public String producer;

		// booking-service may send this, inventory-service won’t
		public UUID eventId;
	}

	public static <T> EventEnvelope<T> of(String eventType, int version, UUID correlationId, String producer, T data) {
		EventEnvelope<T> env = new EventEnvelope<>();
		Meta m = new Meta();
		m.eventId = UUID.randomUUID();
		m.eventType = eventType;
		m.eventVersion = version;
		m.correlationId = correlationId;
		m.producer = producer;
		m.occurredAt = Instant.now();
		env.meta = m;
		env.data = data;
		return env;
	}
}