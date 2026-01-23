package com.letzautomate.inventory.infrastructure.messaging;

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

	@Value("${spring.kafka.bootstrap-servers}")
	private String bootstrapServers;

	@Value("${spring.kafka.consumer.group-id:inventory-service}")
	private String groupId;

	// ------------------------
	// Producer (Inventory -> Kafka)
	// ------------------------
	@Bean
	public ProducerFactory<String, Object> producerFactory() {
		Map<String, Object> props = new HashMap<>();

		props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
		props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);

		// JSON value
		props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);

		// IMPORTANT: avoid type headers across services (class names differ)
		props.put(JsonSerializer.ADD_TYPE_INFO_HEADERS, false);

		return new DefaultKafkaProducerFactory<>(props);
	}

	@Bean
	public KafkaTemplate<String, Object> kafkaTemplate(ProducerFactory<String, Object> pf) {
		return new KafkaTemplate<>(pf);
	}

	// ------------------------
	// Consumer (Kafka -> Inventory)
	// One consumer factory is enough because both topics deserialize into EventEnvelope
	// ------------------------
	@Bean
	public ConsumerFactory<String, EventEnvelope<?>> eventEnvelopeConsumerFactory() {
		Map<String, Object> props = new HashMap<>();

		props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
		props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);

		// Keep latest for normal runs; switch to earliest temporarily if you want replay.
		props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");

		// ErrorHandlingDeserializer wrapping JsonDeserializer
		props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class);
		props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class);

		props.put(ErrorHandlingDeserializer.KEY_DESERIALIZER_CLASS, StringDeserializer.class);
		props.put(ErrorHandlingDeserializer.VALUE_DESERIALIZER_CLASS, JsonDeserializer.class);

		// JsonDeserializer settings:
		// - Don't rely on producer type headers
		// - Deserialize into EventEnvelope (data becomes LinkedHashMap)
		props.put(JsonDeserializer.USE_TYPE_INFO_HEADERS, false);
		props.put(JsonDeserializer.VALUE_DEFAULT_TYPE,
				"com.letzautomate.inventory.infrastructure.messaging.EventEnvelope");
		props.put(JsonDeserializer.TRUSTED_PACKAGES, "*");

		return new DefaultKafkaConsumerFactory<>(props);
	}

	/**
	 * This is the factory name Spring will look for if your listener uses:
	 * containerFactory = "defaultListenerFactory"
	 */
	@Bean(name = "defaultListenerFactory")
	public ConcurrentKafkaListenerContainerFactory<String, EventEnvelope<?>> defaultListenerFactory(
			ConsumerFactory<String, EventEnvelope<?>> eventEnvelopeConsumerFactory
	) {
		ConcurrentKafkaListenerContainerFactory<String, EventEnvelope<?>> factory =
				new ConcurrentKafkaListenerContainerFactory<>();

		factory.setConsumerFactory(eventEnvelopeConsumerFactory);

		// Keep it simple: batch ack is fine.
		factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.BATCH);

		return factory;
	}

	/**
	 * Keep your existing name too, so InventoryProcessor can reference it for payment listener
	 * without changing code.
	 */
	@Bean(name = "paymentSucceededListenerFactory")
	public ConcurrentKafkaListenerContainerFactory<String, EventEnvelope<?>> paymentSucceededListenerFactory(
			ConsumerFactory<String, EventEnvelope<?>> eventEnvelopeConsumerFactory
	) {
		ConcurrentKafkaListenerContainerFactory<String, EventEnvelope<?>> factory =
				new ConcurrentKafkaListenerContainerFactory<>();

		factory.setConsumerFactory(eventEnvelopeConsumerFactory);
		factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.BATCH);

		return factory;
	}
}