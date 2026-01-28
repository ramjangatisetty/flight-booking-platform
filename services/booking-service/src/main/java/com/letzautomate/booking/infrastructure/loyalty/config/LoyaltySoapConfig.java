package com.letzautomate.booking.infrastructure.loyalty.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.ws.client.core.WebServiceTemplate;
import org.springframework.ws.transport.http.ClientHttpRequestMessageSender;
import org.springframework.http.client.SimpleClientHttpRequestFactory;

@Configuration
public class LoyaltySoapConfig {

	@Value("${loyalty.soap.endpoint}")
	private String loyaltyEndpoint;

	@Value("${loyalty.soap.connect-timeout:5000}")
	private int connectTimeout;

	@Value("${loyalty.soap.read-timeout:10000}")
	private int readTimeout;

	@Bean
	public ClientHttpRequestMessageSender loyaltyMessageSender() {
		SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
		requestFactory.setConnectTimeout(connectTimeout);
		requestFactory.setReadTimeout(readTimeout);
		
		ClientHttpRequestMessageSender messageSender = new ClientHttpRequestMessageSender();
		messageSender.setRequestFactory(requestFactory);
		return messageSender;
	}

	@Bean
	public WebServiceTemplate loyaltyWebServiceTemplate(ClientHttpRequestMessageSender messageSender) {
		WebServiceTemplate webServiceTemplate = new WebServiceTemplate();
		webServiceTemplate.setDefaultUri(loyaltyEndpoint);
		webServiceTemplate.setMessageSender(messageSender);
		return webServiceTemplate;
	}
}
