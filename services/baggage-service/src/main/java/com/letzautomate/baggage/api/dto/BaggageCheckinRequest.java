package com.letzautomate.baggage.api.dto;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

@JacksonXmlRootElement(localName = "BaggageCheckinRequest", namespace = "http://letzautomate.com/baggage/v1")
public class BaggageCheckinRequest {

	@JacksonXmlProperty(localName = "bookingId", namespace = "http://letzautomate.com/baggage/v1")
	@NotBlank(message = "bookingId is required")
	private String bookingId;

	@JacksonXmlProperty(localName = "passengerId", namespace = "http://letzautomate.com/baggage/v1")
	@NotBlank(message = "passengerId is required")
	private String passengerId;

	@JacksonXmlProperty(localName = "bagTag", namespace = "http://letzautomate.com/baggage/v1")
	@NotBlank(message = "bagTag is required")
	@Pattern(regexp = "[A-Z]{2}[0-9]{8}", message = "bagTag must match pattern [A-Z]{2}[0-9]{8}")
	private String bagTag;

	@JacksonXmlProperty(localName = "origin", namespace = "http://letzautomate.com/baggage/v1")
	@NotBlank(message = "origin is required")
	@Pattern(regexp = "[A-Z]{3}", message = "origin must be a 3-letter airport code")
	private String origin;

	@JacksonXmlProperty(localName = "destination", namespace = "http://letzautomate.com/baggage/v1")
	@NotBlank(message = "destination is required")
	@Pattern(regexp = "[A-Z]{3}", message = "destination must be a 3-letter airport code")
	private String destination;

	public BaggageCheckinRequest() {}

	public BaggageCheckinRequest(String bookingId, String passengerId, String bagTag, String origin, String destination) {
		this.bookingId = bookingId;
		this.passengerId = passengerId;
		this.bagTag = bagTag;
		this.origin = origin;
		this.destination = destination;
	}

	public String getBookingId() {
		return bookingId;
	}

	public void setBookingId(String bookingId) {
		this.bookingId = bookingId;
	}

	public String getPassengerId() {
		return passengerId;
	}

	public void setPassengerId(String passengerId) {
		this.passengerId = passengerId;
	}

	public String getBagTag() {
		return bagTag;
	}

	public void setBagTag(String bagTag) {
		this.bagTag = bagTag;
	}

	public String getOrigin() {
		return origin;
	}

	public void setOrigin(String origin) {
		this.origin = origin;
	}

	public String getDestination() {
		return destination;
	}

	public void setDestination(String destination) {
		this.destination = destination;
	}
}
