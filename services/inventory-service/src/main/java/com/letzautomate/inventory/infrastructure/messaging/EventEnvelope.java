package com.letzautomate.inventory.infrastructure.messaging;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.TextNode;
import java.time.Instant;
import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
public class EventEnvelope<T> {
	public Meta meta;
	public T data;

	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class Meta {

		// Accept either "eventName" or "eventType"
		@JsonAlias({"eventType"})
		public String eventName;

		// Accept either "version" or "eventVersion"
		@JsonAlias({"eventVersion"})
		public int version;

		/**
		 * occurredAt differs between services:
		 * - sometimes ISO string
		 * - sometimes epoch seconds / millis
		 */
		public JsonNode occurredAt;

		/**
		 * correlationId differs:
		 * - sometimes UUID
		 * - sometimes string
		 */
		public JsonNode correlationId;

		public String producer;

		public Meta() {}

		/**
		 * Outbound-friendly constructor (sets JsonNode fields correctly).
		 */
		public Meta(String eventName, int version, Instant occurredAt, UUID correlationId, String producer) {
			this.eventName = eventName;
			this.version = version;
			this.occurredAt = (occurredAt == null) ? null : new TextNode(occurredAt.toString());
			this.correlationId = (correlationId == null) ? null : new TextNode(correlationId.toString());
			this.producer = producer;
		}

		public UUID correlationIdAsUuid() {
			if (correlationId == null || correlationId.isNull()) return null;

			if (correlationId.isTextual()) {
				try {
					return UUID.fromString(correlationId.asText());
				} catch (Exception e) {
					return null;
				}
			}
			return null;
		}

		public Instant occurredAtAsInstant() {
			if (occurredAt == null || occurredAt.isNull()) return null;

			if (occurredAt.isTextual()) {
				try {
					return Instant.parse(occurredAt.asText());
				} catch (Exception e) {
					return null;
				}
			}

			if (occurredAt.isNumber()) {
				long n = occurredAt.asLong();
				if (n > 10_000_000_000L) return Instant.ofEpochMilli(n);
				return Instant.ofEpochSecond(n);
			}

			return null;
		}
	}

	public EventEnvelope() {}

	/**
	 * Outbound helper for inventory-service events.
	 */
	public static <T> EventEnvelope<T> of(String eventName, int version, UUID correlationId, String producer, T data) {
		EventEnvelope<T> env = new EventEnvelope<>();
		env.meta = new Meta(eventName, version, Instant.now(), correlationId, producer);
		env.data = data;
		return env;
	}
}