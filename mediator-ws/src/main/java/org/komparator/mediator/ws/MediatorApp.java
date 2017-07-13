package org.komparator.mediator.ws;

import org.komparator.security.KeystoreAliasManager;

public class MediatorApp {

	public static String wsSecondaryMediator = "http://localhost:8072/mediator-ws/endpoint";

	public static void main(String[] args) throws Exception {
		// Check arguments
		if (args.length == 0 || args.length == 2) {
			System.err.println("Argument(s) missing!");
			System.err.println("Usage: java " + MediatorApp.class.getName() + " wsURL OR uddiURL wsName wsURL");
			return;
		}
		
		String uddiURL = null;
		String wsName = null;
		String wsURL = null;
		String ccURL = null;
		int timeout = 0;

		
		// Create server implementation object, according to options
		MediatorEndpointManager endpoint = null;
		if (args.length == 1) {
			wsURL = args[0];
			endpoint = new MediatorEndpointManager(wsURL);
		} else if (args.length >= 3) {
			uddiURL = args[0];
			wsName = args[1];
			wsURL = args[2];
			ccURL = args[3];
			endpoint = new MediatorEndpointManager(uddiURL, wsName, wsURL, ccURL);
			endpoint.setVerbose(true);
			timeout = Integer.parseInt(args[4]);
			
			
            //Saves wsname for usage with Handlers
            KeystoreAliasManager.getInstance().setWsName(wsName);
		}
		

		try {
			endpoint.start();
			if(endpoint.isPrimary()){
				System.out.println(endpoint.getPort().ping("this"));
				LifeProof lifeProof = new LifeProof((Object)new Integer(timeout), (Object)wsSecondaryMediator);
				lifeProof.setDaemon(true);
				lifeProof.start();
			}
			else{
				LifeProof lifeProof = new LifeProof((Object)new Integer(timeout), (Object)endpoint);
				lifeProof.setDaemon(true);
				lifeProof.start();
			}
			
			//lifeProof.run();
			//endpoint.getPort().clear();			
			endpoint.awaitConnections();
		} finally {
			endpoint.stop();
		}

	}

}
