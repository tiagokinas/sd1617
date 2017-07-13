package org.komparator.mediator.ws.cli;

import org.komparator.security.KeystoreAliasManager;

import java.net.SocketTimeoutException;

import java.util.*;
import javax.xml.ws.*;

public class MediatorClientApp {

    public static void main(String[] args) throws Exception {
        // Check arguments
        if (args.length == 0) {
            System.err.println("Argument(s) missing!");
            System.err.println("Usage: java " + MediatorClientApp.class.getName()
                + " wsURL OR uddiURL wsName");
            return;
        }
        String uddiURL = null;
        String wsName = null;
        String wsURL = null;
        if (args.length == 1) {
            wsURL = args[0];
        } else if (args.length >= 2) {
            uddiURL = args[0];
            wsName = args[1];
        }
        
		//Saves wsname for usage with Handlers
		KeystoreAliasManager.getInstance().setWsName(wsName);
        
        // Create client
        MediatorClient client = null;

        if (wsURL != null) {
            System.out.printf("Creating client for server at %s%n", wsURL);
            client = new MediatorClient(wsURL);
        } else if (uddiURL != null) {
            System.out.printf("Creating client using UDDI at %s for server with name %s%n",
                uddiURL, wsName);
            client = new MediatorClient(uddiURL, wsName);
        }

        // the following remote invocations are just basic examples
        // the actual tests are made using JUnit
        try {
            System.out.println("Invoke ping()...");
            String result = client.ping("pinga 1");
            /*String res2 = client.ping("pinga 2"); --- Front End Test*/
            System.out.println(result);
            /*System.out.println(res2);  --- Front End Test*/
        } catch(WebServiceException wse) {
            System.out.println("Caught: " + wse);
            Throwable cause = wse.getCause();
            if (cause != null && cause instanceof SocketTimeoutException) {
                System.out.println("The cause was a timeout exception: " + cause);
            }
        }

    }
}
