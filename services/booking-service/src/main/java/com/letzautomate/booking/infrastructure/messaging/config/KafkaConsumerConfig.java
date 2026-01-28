package com.letzautomate.booking.infrastructure.messaging.config;

import com.letzautomate.booking.infrastructure.messaging.event.EventEnvelope;
import java.util.HashMap;
import java.util.Map;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import org.springframework.kafka.support.serializer.JsonDeserializer;

@Configuration
public class KafkaConsumerConfig {

	@Value("${kafka.bootstrap-servers}")
	private String bootstrapServers;

	@Value("${spring.kafka.consumer.group-id:booking-service}")
	private String groupId;

	@Bean
	public ConsumerFactory<String, EventEnvelope> inventoryConsumerFactory() {

		Map<String, Object> props = new HashMap<>();
		props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
		props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
		props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");

		// Key deserializer class is fine here
		props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);

		// IMPORTANT:
		// Do NOT set JsonDeserializer config props here if you are configuring via setters below.
		// Also do NOT set VALUE_DESERIALIZER_CLASS_CONFIG to ErrorHandlingDeserializer.class
		// because we're providing the value deserializer instance directly.

		JsonDeserializer<EventEnvelope> jsonDeserializer = new JsonDeserializer<>(EventEnvelope.class);
		jsonDeserializer.addTrustedPackages("*");
		jsonDeserializer.setUseTypeHeaders(false);

		ErrorHandlingDeserializer<EventEnvelope> valueDeserializer =
				new ErrorHandlingDeserializer<>(jsonDeserializer);

		return new DefaultKafkaConsumerFactory<>(
				props,
				new StringDeserializer(),
				valueDeserializer
		);
	}

	@Bean(name = "inventoryReservedListenerFactory")
	public ConcurrentKafkaListenerContainerFactory<String, EventEnvelope> inventoryReservedListenerFactory(
			ConsumerFactory<String, EventEnvelope> inventoryConsumerFactory) {

		ConcurrentKafkaListenerContainerFactory<String, EventEnvelope> factory =
				new ConcurrentKafkaListenerContainerFactory<>();

		factory.setConsumerFactory(inventoryConsumerFactory);
		factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.BATCH);

		return factory;
	}

	@Bean(name = "paymentListenerFactory")
	public ConcurrentKafkaListenerContainerFactory<String, EventEnvelope> paymentListenerFactory(
			ConsumerFactory<String, EventEnvelope> inventoryConsumerFactory) {

		ConcurrentKafkaListenerContainerFactory<String, EventEnvelope> factory =
				new ConcurrentKafkaListenerContainerFactory<>();

		factory.setConsumerFactory(inventoryConsumerFactory);
		factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.BATCH);

		return factory;
	}

	@Bean(name = "baggageListenerFactory")
	public ConcurrentKafkaListenerContainerFactory<String, EventEnvelope> baggageListenerFactory(
			ConsumerFactory<String, EventEnvelope> inventoryConsumerFactory) {

		ConcurrentKafkaListenerContainerFactory<String, EventEnvelope> factory =
				new ConcurrentKafkaListenerContainerFactory<>();

		factory.setConsumerFactory(inventoryConsumerFactory);
		factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.RECORD);

		return factory;
	}
}
