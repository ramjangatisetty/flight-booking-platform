package com.letzautomate.booking.infrastructure.loyalty;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.ws.client.core.WebServiceTemplate;
import org.springframework.ws.soap.client.SoapFaultClientException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.math.BigDecimal;
import java.util.UUID;

@Component
public class LoyaltySoapClient {

	private static final Logger log = LoggerFactory.getLogger(LoyaltySoapClient.class);
	private static final String NAMESPACE_URI = "http://letzautomate.com/loyalty/v1";
	private static final String SOAP_ACTION = "http://letzautomate.com/loyalty/v1/AccruePoints";

	private final WebServiceTemplate webServiceTemplate;
	private final DocumentBuilderFactory documentBuilderFactory;

	public LoyaltySoapClient(WebServiceTemplate loyaltyWebServiceTemplate) {
		this.webServiceTemplate = loyaltyWebServiceTemplate;
		this.documentBuilderFactory = DocumentBuilderFactory.newInstance();
		this.documentBuilderFactory.setNamespaceAware(true);
	}

	public AccruePointsResponse accruePoints(UUID memberId, UUID bookingId, BigDecimal amount, 
	                                          String currency, UUID correlationId) {
		long startTime = System.currentTimeMillis();
		
		try {
			log.info("Calling loyalty AccruePoints SOAP operation. memberId={}, bookingId={}, amount={}, currency={}, correlationId={}",
					memberId, bookingId, amount, currency, correlationId);

			// Build SOAP request
			Element request = buildAccruePointsRequest(memberId, bookingId, amount, currency, correlationId);

			// Call SOAP service using sendSourceAndReceive (no marshalling)
			AccruePointsResponse accrualResult = webServiceTemplate.sendSourceAndReceive(
					new javax.xml.transform.dom.DOMSource(request),
					message -> {
						// Set SOAP Action header
						if (message instanceof org.springframework.ws.soap.SoapMessage) {
							((org.springframework.ws.soap.SoapMessage) message).setSoapAction(SOAP_ACTION);
						}
					},
					responseSource -> {
						// Convert Source to Element
						javax.xml.transform.dom.DOMResult result = new javax.xml.transform.dom.DOMResult();
						javax.xml.transform.TransformerFactory.newInstance().newTransformer()
								.transform(responseSource, result);
						Element response = ((org.w3c.dom.Document) result.getNode()).getDocumentElement();
						
						// Parse and return response
						return parseAccruePointsResponse(response);
					}
			);

			long duration = System.currentTimeMillis() - startTime;
			log.info("Loyalty AccruePoints succeeded. memberId={}, bookingId={}, pointsCredited={}, newBalance={}, duration={}ms",
					accrualResult.memberId, accrualResult.bookingId, accrualResult.pointsCredited, accrualResult.newPointsBalance, duration);

			return accrualResult;

		} catch (SoapFaultClientException e) {
			long duration = System.currentTimeMillis() - startTime;
			log.error("Loyalty AccruePoints SOAP fault. memberId={}, bookingId={}, faultCode={}, faultString={}, duration={}ms",
					memberId, bookingId, e.getFaultCode(), e.getFaultStringOrReason(), duration);
			throw new LoyaltyServiceException("SOAP_FAULT", e.getFaultStringOrReason(), e);
		} catch (Exception e) {
			long duration = System.currentTimeMillis() - startTime;
			log.error("Loyalty AccruePoints failed. memberId={}, bookingId={}, error={}, duration={}ms",
					memberId, bookingId, e.getMessage(), duration, e);
			throw new LoyaltyServiceException("COMMUNICATION_ERROR", "Failed to communicate with loyalty service", e);
		}
	}

	private Element buildAccruePointsRequest(UUID memberId, UUID bookingId, BigDecimal amount, 
	                                          String currency, UUID correlationId) throws Exception {
		DocumentBuilder builder = documentBuilderFactory.newDocumentBuilder();
		Document doc = builder.newDocument();

		Element request = doc.createElementNS(NAMESPACE_URI, "AccruePointsRequest");

		Element memberIdEl = doc.createElementNS(NAMESPACE_URI, "memberId");
		memberIdEl.setTextContent(memberId.toString());
		request.appendChild(memberIdEl);

		Element bookingIdEl = doc.createElementNS(NAMESPACE_URI, "bookingId");
		bookingIdEl.setTextContent(bookingId.toString());
		request.appendChild(bookingIdEl);

		Element amountEl = doc.createElementNS(NAMESPACE_URI, "amount");
		amountEl.setTextContent(amount.toString());
		request.appendChild(amountEl);

		Element currencyEl = doc.createElementNS(NAMESPACE_URI, "currency");
		currencyEl.setTextContent(currency);
		request.appendChild(currencyEl);

		if (correlationId != null) {
			Element correlationIdEl = doc.createElementNS(NAMESPACE_URI, "correlationId");
			correlationIdEl.setTextContent(correlationId.toString());
			request.appendChild(correlationIdEl);
		}

		return request;
	}

	private AccruePointsResponse parseAccruePointsResponse(Element response) {
		String memberId = getElementValue(response, "memberId");
		String bookingId = getElementValue(response, "bookingId");
		String pointsCredited = getElementValue(response, "pointsCredited");
		String newPointsBalance = getElementValue(response, "newPointsBalance");
		String tier = getElementValue(response, "tier");
		String status = getElementValue(response, "status");

		return new AccruePointsResponse(
				UUID.fromString(memberId),
				UUID.fromString(bookingId),
				Integer.parseInt(pointsCredited),
				Integer.parseInt(newPointsBalance),
				tier,
				status
		);
	}

	private String getElementValue(Element parent, String tagName) {
		NodeList nodeList = parent.getElementsByTagNameNS(NAMESPACE_URI, tagName);
		if (nodeList.getLength() > 0) {
			Node node = nodeList.item(0);
			return node.getTextContent();
		}
		return null;
	}

	public static class AccruePointsResponse {
		public final UUID memberId;
		public final UUID bookingId;
		public final int pointsCredited;
		public final int newPointsBalance;
		public final String tier;
		public final String status;

		public AccruePointsResponse(UUID memberId, UUID bookingId, int pointsCredited, 
		                             int newPointsBalance, String tier, String status) {
			this.memberId = memberId;
			this.bookingId = bookingId;
			this.pointsCredited = pointsCredited;
			this.newPointsBalance = newPointsBalance;
			this.tier = tier;
			this.status = status;
		}
	}

	public static class LoyaltyServiceException extends RuntimeException {
		private final String errorCode;

		public LoyaltyServiceException(String errorCode, String message, Throwable cause) {
			super(message, cause);
			this.errorCode = errorCode;
		}

		public String getErrorCode() {
			return errorCode;
		}
	}
}
