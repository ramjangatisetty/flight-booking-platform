package com.letzautomate.inventory.infrastructure.messaging.config;

import com.letzautomate.inventory.infrastructure.messaging.event.EventEnvelope;
import java.util.HashMap;
import java.util.Map;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.*;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;

@Configuration
public class KafkaConfig {

	/**
	 * IMPORTANT:
	 * - Default to localhost:9092 so the service can boot even on "default" profile.
	 * - You can still override via application.yml / application-local.yml.
	 */
	@Value("${spring.kafka.bootstrap-servers:localhost:9092}")
	private String bootstrapServers;

	// -------------------------
	// Producer (Inventory -> Kafka)
	// -------------------------
	@Bean
	public ProducerFactory<String, Object> producerFactory() {
		Map<String, Object> props = new HashMap<>();
		props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
		props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
		props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
		return new DefaultKafkaProducerFactory<>(props);
	}

	@Bean
	public KafkaTemplate<String, Object> kafkaTemplate(ProducerFactory<String, Object> pf) {
		return new KafkaTemplate<>(pf);
	}

	// -------------------------
	// Consumer (Booking Created + Payment Succeeded -> Inventory)
	// -------------------------
	@Bean
	public ConsumerFactory<String, EventEnvelope> paymentSucceededConsumerFactory(
			@Value("${spring.kafka.consumer.group-id:inventory-service}") String groupId,
			@Value("${spring.kafka.consumer.auto-offset-reset:earliest}") String autoOffsetReset
	) {
		Map<String, Object> props = new HashMap<>();
		props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
		props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
		props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, autoOffsetReset);

		// Use ErrorHandlingDeserializer wrappers
		props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class);
		props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class);

		// Delegates
		props.put(ErrorHandlingDeserializer.KEY_DESERIALIZER_CLASS, StringDeserializer.class);
		props.put(ErrorHandlingDeserializer.VALUE_DESERIALIZER_CLASS, JsonDeserializer.class);

		// JsonDeserializer configured via setters
		JsonDeserializer<EventEnvelope> jsonDeserializer = new JsonDeserializer<>(EventEnvelope.class);
		jsonDeserializer.addTrustedPackages("*");
		jsonDeserializer.setUseTypeHeaders(false);

		ErrorHandlingDeserializer<EventEnvelope> valueDeserializer =
				new ErrorHandlingDeserializer<>(jsonDeserializer);

		ErrorHandlingDeserializer<String> keyDeserializer =
				new ErrorHandlingDeserializer<>(new StringDeserializer());

		return new DefaultKafkaConsumerFactory<>(props, keyDeserializer, valueDeserializer);
	}

	@Bean(name = "paymentSucceededListenerFactory")
	public ConcurrentKafkaListenerContainerFactory<String, EventEnvelope> paymentSucceededListenerFactory(
			ConsumerFactory<String, EventEnvelope> paymentSucceededConsumerFactory
	) {
		ConcurrentKafkaListenerContainerFactory<String, EventEnvelope> factory =
				new ConcurrentKafkaListenerContainerFactory<>();
		factory.setConsumerFactory(paymentSucceededConsumerFactory);
		return factory;
	}
}