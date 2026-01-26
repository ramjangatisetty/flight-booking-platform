package com.letzautomate.payment.infrastructure.messaging;

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
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;

@Configuration
public class KafkaConfig {

	/**
	 * Supports either:
	 *  - kafka.bootstrap-servers (your custom property used in booking-service)
	 *  - spring.kafka.bootstrap-servers (Spring standard)
	 */
	@Value("${kafka.bootstrap-servers:${spring.kafka.bootstrap-servers}}")
	private String bootstrapServers;

	@Value("${spring.kafka.consumer.group-id:payment-service}")
	private String groupId;

	// ------------------------
	// Producer (Payment -> Kafka)
	// ------------------------
	@Bean
	public ProducerFactory<String, Object> producerFactory() {
		Map<String, Object> props = new HashMap<>();
		props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
		props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
		props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);

		// IMPORTANT: avoid class/type headers across services
		props.put(JsonSerializer.ADD_TYPE_INFO_HEADERS, false);

		return new DefaultKafkaProducerFactory<>(props);
	}

	@Bean
	public KafkaTemplate<String, Object> kafkaTemplate(ProducerFactory<String, Object> producerFactory) {
		return new KafkaTemplate<>(producerFactory);
	}

	// ------------------------
	// Consumer (Kafka -> Payment)
	// booking.created.v1 -> PaymentProcessor
	// ------------------------
	@Bean
	public ConsumerFactory<String, EventEnvelope<?>> consumerFactory() {

		Map<String, Object> props = new HashMap<>();
		props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
		props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);

		// For local debugging you can temporarily switch to "earliest"
		props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");

		// Use ErrorHandlingDeserializer wrapping JsonDeserializer
		props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
		props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class);

		props.put(ErrorHandlingDeserializer.KEY_DESERIALIZER_CLASS, StringDeserializer.class);
		props.put(ErrorHandlingDeserializer.VALUE_DESERIALIZER_CLASS, JsonDeserializer.class);

		// DO NOT set JsonDeserializer properties here if you are going to configure it via setters.
		// We'll configure the JsonDeserializer instance explicitly below.

		JsonDeserializer<EventEnvelope<?>> jsonDeserializer =
				new JsonDeserializer<>((Class<EventEnvelope<?>>) (Class<?>) EventEnvelope.class);

		// IMPORTANT: don't depend on producer type headers across services
		jsonDeserializer.setUseTypeHeaders(false);
		jsonDeserializer.addTrustedPackages("*");

		ErrorHandlingDeserializer<EventEnvelope<?>> valueDeserializer =
				new ErrorHandlingDeserializer<>(jsonDeserializer);

		return new DefaultKafkaConsumerFactory<>(
				props,
				new StringDeserializer(),
				valueDeserializer
		);
	}

	/**
	 * This bean name is IMPORTANT.
	 * If your @KafkaListener doesn't specify containerFactory,
	 * Spring looks for a bean named "kafkaListenerContainerFactory".
	 */
	@Bean(name = "kafkaListenerContainerFactory")
	public ConcurrentKafkaListenerContainerFactory<String, EventEnvelope<?>> kafkaListenerContainerFactory(
			ConsumerFactory<String, EventEnvelope<?>> consumerFactory) {

		ConcurrentKafkaListenerContainerFactory<String, EventEnvelope<?>> factory =
				new ConcurrentKafkaListenerContainerFactory<>();

		factory.setConsumerFactory(consumerFactory);

		// Simple mode for now; you can adjust later
		factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.BATCH);

		return factory;
	}
}