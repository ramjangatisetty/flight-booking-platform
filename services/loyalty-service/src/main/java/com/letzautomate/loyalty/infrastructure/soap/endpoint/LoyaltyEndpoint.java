package com.letzautomate.loyalty.infrastructure.soap.endpoint;

import com.letzautomate.loyalty.application.LoyaltyService;
import com.letzautomate.loyalty.infrastructure.persistence.entity.LoyaltyMemberEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.server.endpoint.annotation.PayloadRoot;
import org.springframework.ws.server.endpoint.annotation.RequestPayload;
import org.springframework.ws.server.endpoint.annotation.ResponsePayload;
import org.springframework.ws.soap.server.endpoint.annotation.SoapFault;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.util.UUID;

@Endpoint
public class LoyaltyEndpoint {

	private static final Logger log = LoggerFactory.getLogger(LoyaltyEndpoint.class);
	private static final String NAMESPACE_URI = "http://letzautomate.com/loyalty/v1";

	private final LoyaltyService loyaltyService;
	private final DocumentBuilderFactory documentBuilderFactory;

	public LoyaltyEndpoint(LoyaltyService loyaltyService) {
		this.loyaltyService = loyaltyService;
		this.documentBuilderFactory = DocumentBuilderFactory.newInstance();
		this.documentBuilderFactory.setNamespaceAware(true);
	}

	@PayloadRoot(namespace = NAMESPACE_URI, localPart = "EnrollMemberRequest")
	@ResponsePayload
	public Element enrollMember(@RequestPayload Element request) {
		try {
			log.info("Received EnrollMember request");

			// Extract request data
			String firstName = getElementValue(request, "firstName");
			String lastName = getElementValue(request, "lastName");
			String email = getElementValue(request, "email");

			// Validate input
			if (firstName == null || firstName.isBlank() ||
					lastName == null || lastName.isBlank() ||
					email == null || email.isBlank()) {
				throw new LoyaltyFaultException("VALIDATION_ERROR", "firstName, lastName, and email are required");
			}

			// Enroll member
			LoyaltyMemberEntity member = loyaltyService.enrollMember(firstName, lastName, email);

			// Build response
			DocumentBuilder builder = documentBuilderFactory.newDocumentBuilder();
			org.w3c.dom.Document doc = builder.newDocument();

			Element response = doc.createElementNS(NAMESPACE_URI, "EnrollMemberResponse");
			
			Element memberIdEl = doc.createElementNS(NAMESPACE_URI, "memberId");
			memberIdEl.setTextContent(member.getMemberId().toString());
			response.appendChild(memberIdEl);

			Element tierEl = doc.createElementNS(NAMESPACE_URI, "tier");
			tierEl.setTextContent(member.getTier());
			response.appendChild(tierEl);

			Element statusEl = doc.createElementNS(NAMESPACE_URI, "status");
			statusEl.setTextContent(member.getStatus());
			response.appendChild(statusEl);

			log.info("Member enrolled successfully: memberId={}, tier={}", member.getMemberId(), member.getTier());
			return response;

		} catch (LoyaltyFaultException e) {
			throw e;
		} catch (IllegalArgumentException e) {
			throw new LoyaltyFaultException("VALIDATION_ERROR", e.getMessage());
		} catch (Exception e) {
			log.error("Error enrolling member", e);
			throw new LoyaltyFaultException("INTERNAL_ERROR", "An error occurred while enrolling member");
		}
	}

	@PayloadRoot(namespace = NAMESPACE_URI, localPart = "GetMemberStatusRequest")
	@ResponsePayload
	public Element getMemberStatus(@RequestPayload Element request) {
		try {
			log.info("Received GetMemberStatus request");

			// Extract request data
			String memberIdStr = getElementValue(request, "memberId");

			// Validate input
			if (memberIdStr == null || memberIdStr.isBlank()) {
				throw new LoyaltyFaultException("VALIDATION_ERROR", "memberId is required");
			}

			UUID memberId;
			try {
				memberId = UUID.fromString(memberIdStr);
			} catch (IllegalArgumentException e) {
				throw new LoyaltyFaultException("VALIDATION_ERROR", "Invalid memberId format");
			}

			// Get member status
			LoyaltyMemberEntity member = loyaltyService.getMemberStatus(memberId);

			// Build response
			DocumentBuilder builder = documentBuilderFactory.newDocumentBuilder();
			org.w3c.dom.Document doc = builder.newDocument();

			Element response = doc.createElementNS(NAMESPACE_URI, "GetMemberStatusResponse");

			Element memberIdEl = doc.createElementNS(NAMESPACE_URI, "memberId");
			memberIdEl.setTextContent(member.getMemberId().toString());
			response.appendChild(memberIdEl);

			Element tierEl = doc.createElementNS(NAMESPACE_URI, "tier");
			tierEl.setTextContent(member.getTier());
			response.appendChild(tierEl);

			Element statusEl = doc.createElementNS(NAMESPACE_URI, "status");
			statusEl.setTextContent(member.getStatus());
			response.appendChild(statusEl);

			Element pointsEl = doc.createElementNS(NAMESPACE_URI, "pointsBalance");
			pointsEl.setTextContent(String.valueOf(member.getPointsBalance()));
			response.appendChild(pointsEl);

			log.info("Member status retrieved: memberId={}, tier={}, points={}", 
					member.getMemberId(), member.getTier(), member.getPointsBalance());
			return response;

		} catch (LoyaltyFaultException e) {
			throw e;
		} catch (LoyaltyService.MemberNotFoundException e) {
			throw new LoyaltyFaultException("MEMBER_NOT_FOUND", e.getMessage());
		} catch (Exception e) {
			log.error("Error getting member status", e);
			throw new LoyaltyFaultException("INTERNAL_ERROR", "An error occurred while getting member status");
		}
	}

	@PayloadRoot(namespace = NAMESPACE_URI, localPart = "AccruePointsRequest")
	@ResponsePayload
	public Element accruePoints(@RequestPayload Element request) {
		try {
			log.info("Received AccruePoints request");

			// Extract request data
			String memberIdStr = getElementValue(request, "memberId");
			String bookingIdStr = getElementValue(request, "bookingId");
			String amountStr = getElementValue(request, "amount");
			String currency = getElementValue(request, "currency");
			String correlationIdStr = getElementValue(request, "correlationId");

			// Validate input
			if (memberIdStr == null || memberIdStr.isBlank()) {
				throw new LoyaltyFaultException("VALIDATION_ERROR", "memberId is required");
			}
			if (bookingIdStr == null || bookingIdStr.isBlank()) {
				throw new LoyaltyFaultException("VALIDATION_ERROR", "bookingId is required");
			}
			if (amountStr == null || amountStr.isBlank()) {
				throw new LoyaltyFaultException("VALIDATION_ERROR", "amount is required");
			}
			if (currency == null || currency.isBlank()) {
				throw new LoyaltyFaultException("VALIDATION_ERROR", "currency is required");
			}

			UUID memberId;
			UUID bookingId;
			UUID correlationId = null;
			java.math.BigDecimal amount;

			try {
				memberId = UUID.fromString(memberIdStr);
				bookingId = UUID.fromString(bookingIdStr);
				if (correlationIdStr != null && !correlationIdStr.isBlank()) {
					correlationId = UUID.fromString(correlationIdStr);
				}
				amount = new java.math.BigDecimal(amountStr);
			} catch (IllegalArgumentException e) {
				throw new LoyaltyFaultException("VALIDATION_ERROR", "Invalid UUID or amount format");
			}

			// Accrue points
			LoyaltyService.AccrualResult result = loyaltyService.accruePoints(
					memberId, bookingId, amount, currency, correlationId
			);

			// Build response
			DocumentBuilder builder = documentBuilderFactory.newDocumentBuilder();
			org.w3c.dom.Document doc = builder.newDocument();

			Element response = doc.createElementNS(NAMESPACE_URI, "AccruePointsResponse");

			Element memberIdEl = doc.createElementNS(NAMESPACE_URI, "memberId");
			memberIdEl.setTextContent(result.memberId.toString());
			response.appendChild(memberIdEl);

			Element bookingIdEl = doc.createElementNS(NAMESPACE_URI, "bookingId");
			bookingIdEl.setTextContent(result.bookingId.toString());
			response.appendChild(bookingIdEl);

			Element pointsCreditedEl = doc.createElementNS(NAMESPACE_URI, "pointsCredited");
			pointsCreditedEl.setTextContent(String.valueOf(result.pointsCredited));
			response.appendChild(pointsCreditedEl);

			Element newBalanceEl = doc.createElementNS(NAMESPACE_URI, "newPointsBalance");
			newBalanceEl.setTextContent(String.valueOf(result.newPointsBalance));
			response.appendChild(newBalanceEl);

			Element tierEl = doc.createElementNS(NAMESPACE_URI, "tier");
			tierEl.setTextContent(result.tier);
			response.appendChild(tierEl);

			Element statusEl = doc.createElementNS(NAMESPACE_URI, "status");
			statusEl.setTextContent(result.status);
			response.appendChild(statusEl);

			log.info("Points accrued successfully. memberId={}, bookingId={}, pointsCredited={}, newBalance={}, alreadyAccrued={}",
					result.memberId, result.bookingId, result.pointsCredited, result.newPointsBalance, result.alreadyAccrued);
			return response;

		} catch (LoyaltyFaultException e) {
			throw e;
		} catch (LoyaltyService.MemberNotFoundException e) {
			throw new LoyaltyFaultException("MEMBER_NOT_FOUND", e.getMessage());
		} catch (Exception e) {
			log.error("Error accruing points", e);
			throw new LoyaltyFaultException("INTERNAL_ERROR", "An error occurred while accruing points");
		}
	}

	private String getElementValue(Element parent, String tagName) {
		NodeList nodeList = parent.getElementsByTagNameNS(NAMESPACE_URI, tagName);
		if (nodeList.getLength() > 0) {
			Node node = nodeList.item(0);
			return node.getTextContent();
		}
		return null;
	}

	public static class LoyaltyFaultException extends RuntimeException {
		private final String faultCode;

		public LoyaltyFaultException(String faultCode, String message) {
			super(message);
			this.faultCode = faultCode;
		}

		public String getFaultCode() {
			return faultCode;
		}
	}
}
