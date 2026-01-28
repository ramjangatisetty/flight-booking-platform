package com.letzautomate.baggage.api.dto;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

@JacksonXmlRootElement(localName = "BaggageStatusUpdateRequest")
public class BaggageStatusUpdateRequest {

	@JacksonXmlProperty(localName = "status")
	@NotBlank(message = "Status is required")
	@Pattern(regexp = "CHECKED_IN|LOADED|IN_TRANSIT|ARRIVED|DELIVERED|LOST", 
	         message = "Status must be one of: CHECKED_IN, LOADED, IN_TRANSIT, ARRIVED, DELIVERED, LOST")
	private String status;

	@JacksonXmlProperty(localName = "airport")
	@Pattern(regexp = "[A-Z]{3}", message = "Airport code must be 3 uppercase letters")
	private String airport;

	public BaggageStatusUpdateRequest() {
	}

	public BaggageStatusUpdateRequest(String status, String airport) {
		this.status = status;
		this.airport = airport;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getAirport() {
		return airport;
	}

	public void setAirport(String airport) {
		this.airport = airport;
	}
}
