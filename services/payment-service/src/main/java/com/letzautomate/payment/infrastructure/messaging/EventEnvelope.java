package com.letzautomate.payment.infrastructure.messaging;

import java.time.Instant;
import java.util.UUID;

public class EventEnvelope<T> {
	public Meta meta;
	public T data;

	public static class Meta {
		public UUID eventId;
		public String eventType;
		public int eventVersion;
		public UUID correlationId;
		public String producer;
		public Instant occurredAt;
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
