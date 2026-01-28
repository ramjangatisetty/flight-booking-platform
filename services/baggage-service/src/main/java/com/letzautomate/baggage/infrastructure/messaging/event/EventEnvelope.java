package com.letzautomate.baggage.infrastructure.messaging.event;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.time.Instant;
import java.util.UUID;

/**
 * Unified event envelope for all services.
 * Standardized metadata fields: eventId, eventType, eventVersion, occurredAt, correlationId, producer.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class EventEnvelope<T> {
	public Meta meta;
	public T data;

	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class Meta {
		public UUID eventId;
		public String eventType;
		public int eventVersion;
		public Instant occurredAt;
		public UUID correlationId;
		public String producer;
	}

	public static <T> EventEnvelope<T> of(String eventType, int version, UUID correlationId, String producer, T data) {
		EventEnvelope<T> env = new EventEnvelope<>();
		Meta m = new Meta();
		m.eventId = UUID.randomUUID();
		m.eventType = eventType;
		m.eventVersion = version;
		m.occurredAt = Instant.now();
		m.correlationId = correlationId;
		m.producer = producer;
		env.meta = m;
		env.data = data;
		return env;
	}
}
