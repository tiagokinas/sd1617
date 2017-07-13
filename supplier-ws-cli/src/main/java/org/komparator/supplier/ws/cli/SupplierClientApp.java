package org.komparator.supplier.ws.cli;

import pt.ulisboa.tecnico.sdis.ws.uddi.UDDINaming;
import org.komparator.security.KeystoreAliasManager;

/** Main class that starts the Supplier Web Service client. */
public class SupplierClientApp {


	public static void main(String[] args) throws Exception {
		// Check arguments
		
		if (args.length < 2) {
			System.err.println("Argument(s) missing!");
			System.err.println("Usage: java " + SupplierClientApp.class.getName() + " uddiURL" + "wsName");
			return;
		}
		String uddiURL = args[0];
		String name = args[1];

		//Saves wsname for usage with Handlers
		KeystoreAliasManager.getInstance().setWsName(name);
		
		// Create client
		System.out.printf("Creating client for server at %s%n", uddiURL);
		SupplierClient client = new SupplierClient(uddiURL, name);
		// the following remote invocations are just basic examples
		// the actual tests are made using JUnit

		System.out.println("Invoke ping()...");
		String result = client.ping("client");
		System.out.print("Result: ");
		System.out.println(result);
	}

}
