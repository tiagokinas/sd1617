package org.komparator.security.handler;

import java.util.Iterator;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Set;
import java.io.*;

import javax.xml.namespace.QName;
import javax.xml.soap.*;
import javax.xml.ws.soap.SOAPFaultException;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.MessageContext.Scope;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;
import static javax.xml.bind.DatatypeConverter.*;

import org.komparator.security.*;
import java.security.*;
import javax.crypto.*;

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

public class CreditCardCipherClientHandler implements SOAPHandler<SOAPMessageContext> {

	public static final String REQUEST_PROPERTY = "my.request.property";
	public static final String RESPONSE_PROPERTY = "my.response.property";

	public static final String REQUEST_HEADER = "DateRequestHeader";
	public static final String REQUEST_NS = "urn:example";

	public static final String RESPONSE_HEADER = "DateResponseHeader";
	public static final String RESPONSE_NS = REQUEST_NS;

	public static final String CLASS_NAME = CreditCardCipherClientHandler.class.getSimpleName();
	public static final String TOKEN = "cc-client-handler";

	public boolean handleMessage(SOAPMessageContext smc) {
		Boolean outbound = (Boolean) smc.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);
		if (outbound) {
			try {
				// get SOAP envelope
				SOAPMessage msg = smc.getMessage();
				SOAPPart sp = msg.getSOAPPart();
				SOAPEnvelope se = sp.getEnvelope();
                SOAPBody sb = se.getBody();
                
                CryptoUtil cu = new CryptoUtil();
                
                Iterator it = sb.getChildElements();
                while(it.hasNext()){
                    SOAPElement cc = (SOAPElement) it.next();
                    Iterator it2 = cc.getChildElements();
                    while(it2.hasNext()){
                        SOAPElement elem = (SOAPElement)it2.next();
                        System.out.println(elem.getElementName().getLocalName());
                        if(elem.getElementName().getLocalName().equals("creditCardNr")){
                            try{
                            	String creditCard = elem.getValue();

                            	System.out.println("regs : value to encode " + creditCard);
                            	byte[] bts = creditCard.getBytes();

                            	byte[] btsE = cu.asymCipher(bts);

                            	System.out.println("regs : encoded bytes " + printBase64Binary(btsE));

                                elem.setTextContent(printBase64Binary(btsE));
                            }catch(Exception e){
                                System.out.println(e.getMessage());
                            }
                        }
                    }
                }
			} catch (SOAPException e) {
				System.out.printf("Failed to add SOAP header because of %s%n", e);
			}
		}
		return true;
	}

	public boolean handleFault(SOAPMessageContext smc) {
		System.out.println("CLIENT DETECTED FAULT");
		return true;
	}

	public Set<QName> getHeaders() {
		return null;
	}

	public void close(MessageContext messageContext) {
	}

}
