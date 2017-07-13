package org.komparator.supplier.ws;

import javax.xml.ws.Endpoint;

import pt.ulisboa.tecnico.sdis.ws.uddi.UDDINaming;

import org.komparator.security.KeystoreAliasManager;

/** Main class that starts the Supplier Web Service. */
public class SupplierApp {

	public static void main(String[] args) throws Exception {
		// Check arguments

		if (args.length < 3) {
			System.err.println("Argument(s) missing!");
			System.err.printf("Usage: java %s uddiURL wsName wsURL%n", SupplierApp.class.getName());
			return;
		}

		String uddiURL = args[0];
		String name = args[1];
		String url = args[2];
		
		//Saves name for usage with Handlers
		KeystoreAliasManager.getInstance().setWsName(name);
		
		// Create server implementation object
		SupplierEndpointManager endpoint = new SupplierEndpointManager(uddiURL, name, url);
		try {
			endpoint.start();
			endpoint.awaitConnections();
		} finally {
			endpoint.stop();
		}
	}

}
