package org.komparator.security.handler;

import java.util.Iterator;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Set;


import java.lang.RuntimeException;

import java.text.ParseException;

import javax.xml.namespace.QName;
import javax.xml.soap.Name;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPHeader;
import javax.xml.soap.SOAPHeaderElement;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPPart;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPFault;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.MessageContext.Scope;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;

import org.komparator.security.IdsManager;

/**
 * This is the handler client class of the Relay example.
 *
 * #2 The client handler receives data from the client (via message context). #3
 * The client handler passes data to the server handler (via outbound SOAP
 * message header).
 *
 * *** GO TO server handler to see what happens next! ***
 *
 * #10 The client handler receives data from the server handler (via inbound
 * SOAP message header). #11 The client handler passes data to the client (via
 * message context).
 *
 * *** GO BACK TO client to see what happens next! ***
 */

public class FrontEndServerHandler implements SOAPHandler<SOAPMessageContext> {

	public static final String REQUEST_PROPERTY = "my.request.property";
	public static final String RESPONSE_PROPERTY = "my.response.property";

	public static final String REQUEST_HEADER = "FrontEndRequestHeader";
	public static final String REQUEST_NS = "urn:example";

	public static final String RESPONSE_HEADER = "FrontEndResponseHeader";
	public static final String RESPONSE_NS = REQUEST_NS;

	public static final String CLASS_NAME = FrontEndServerHandler.class.getSimpleName();
	public static final String TOKEN = "frontend-server-handler";

	private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

	public boolean handleMessage(SOAPMessageContext smc) {
		Boolean outbound = (Boolean) smc.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);
		if (outbound) {
			// outbound message

			// *** #2 ***
			// get token from request context
			String propertyValue = (String) smc.get(RESPONSE_PROPERTY);
			System.out.printf("%s received '%s'%n", CLASS_NAME, propertyValue);

			// put token in request SOAP header
			try {
				// get SOAP envelope
				SOAPMessage msg = smc.getMessage();
				SOAPPart sp = msg.getSOAPPart();
				SOAPEnvelope se = sp.getEnvelope();

				// add header
				SOAPHeader sh = se.getHeader();
				if (sh == null)
					sh = se.addHeader();

				// add header element (name, namespace prefix, namespace)
				Name name = se.createName(RESPONSE_HEADER, "e", REQUEST_NS);
				SOAPHeaderElement element = sh.addHeaderElement(name);
				
				try{IdsManager.getInstance().addMessage(IdsManager.getInstance().getLowerKey(), msg);
					System.out.println("Oops... NO KEY!");}
				catch(Exception ex){System.err.println("Oops... NO KEY!");}
				// *** #3 ***
				// add header element value
				//String newValue = propertyValue + "," + TOKEN + " ";
				//element.addTextNode(newValue);

			} catch (SOAPException e) {
				System.out.printf("Failed to add SOAP header because of %s%n", e);
			}

		} else {
			// inbound message

			// get token from response SOAP header
			try {
				// get SOAP envelope header
				SOAPMessage msg = smc.getMessage();
				SOAPPart sp = msg.getSOAPPart();
				SOAPEnvelope se = sp.getEnvelope();
				SOAPHeader sh = se.getHeader();

				// check header
				if (sh == null) {
					System.out.println("Header not found.");
					return true;
				}

				// get first header element
				Name name = se.createName(REQUEST_HEADER, "e", RESPONSE_NS);
				Iterator it = sh.getChildElements(name);
				// check header element
				if (!it.hasNext()) {
					System.out.printf("Header element %s not found.%n", REQUEST_HEADER);
					return true;
				}
				SOAPElement element = (SOAPElement) it.next();

				// *** #10 ***
				// get header element value
				String headerValue = element.getValue();

				if(IdsManager.getInstance().existsMessage(headerValue)){
					SOAPMessage response = IdsManager.getInstance().getMessage(headerValue);
                	smc.setMessage(response);
                	return false;
				}else{
					IdsManager.getInstance().addMessage(headerValue, null);

				System.out.printf("%s got '%s'%n", CLASS_NAME, headerValue);
				}


				} catch (SOAPException e) {
				System.out.printf("Failed to get SOAP header because of %s%n", e);
			}

		}

		return true;
	}

	public boolean handleFault(SOAPMessageContext smc) {
		System.out.println("SERVER DETECTED FAULT");
		return true;
	}

	public Set<QName> getHeaders() {
		return null;
	}

	public void close(MessageContext messageContext) {
	}

}

