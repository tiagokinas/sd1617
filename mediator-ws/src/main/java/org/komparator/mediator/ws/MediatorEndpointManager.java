package org.komparator.mediator.ws;

import java.io.IOException;
import java.util.*;

import javax.xml.ws.Endpoint;

import pt.ulisboa.tecnico.sdis.ws.uddi.UDDINaming;

/** End point manager */
public class MediatorEndpointManager {

	/** UDDI naming server location */
	private String uddiURL = null;
	/** Web Service name */
	private String wsName = null;

	private String ccURL = null;

	/** Web Service location to publish */
	private String wsURL = null;

	/** Port implemenMediatorPortImpltation */
	private MediatorPortImpl portImpl = new MediatorPortImpl(this);

	private Date timestamp = null;

	private boolean primary = false;

	/** Web Service endpoint */
	private Endpoint endpoint = null;
	/** UDDI Naming instance for contacting UDDI server */
	private UDDINaming uddiNaming = null;

	/** output option **/
	private boolean verbose = true;

	/** Obtain Port implementation */
	public MediatorPortImpl getPort() {
		return portImpl;
	}

	/** Get UDDI Naming instance for contacting UDDI server */
	UDDINaming getUddiNaming() {
		return uddiNaming;
	}

	/** Get Web Service UDDI publication name */
	public String getWsName() {
		return wsName;
	}

	public String getCCURL() {
		return ccURL;
	}


	public boolean isVerbose() {
		return verbose;
	}

	public void setVerbose(boolean verbose) {
		this.verbose = verbose;
	}

	public boolean isPrimary() {
		return primary;
	}

	public void setPrimary() {
		primary=true;
	}

	protected void generateNewTimestamp(){
		if (!primary)
			timestamp = new Date();
	}

	public Date getTimestamp(){
		return timestamp;
	}

	public boolean hasTimestamp(){
		return timestamp!=null;
	}

	/** constructor with provided UDDI location, WS name, and WS URL */
	public MediatorEndpointManager(String uddiURL, String wsName, String wsURL, String ccURL) {
		this.uddiURL = uddiURL;
		this.wsName = wsName;
		this.wsURL = wsURL;
		this.ccURL = ccURL;

		if(wsURL.contains("8071"))
			primary = true;
	}

	/** constructor with provided UDDI location, WS name, and WS URL */
	public MediatorEndpointManager(String uddiURL, String wsName, String wsURL) {
		this.uddiURL = uddiURL;
		this.wsName = wsName;
		this.wsURL = wsURL;
	}

	/** constructor with provided web service URL */
	public MediatorEndpointManager(String wsURL) {
		if (wsURL == null)
			throw new NullPointerException("Web Service URL cannot be null!");
		this.wsURL = wsURL;
	}

	/* end point management */

	public void start() throws Exception {
		try{
			endpoint = Endpoint.create(this.portImpl);
			if (verbose) {
				System.out.printf("Starting %s%n", wsURL);
			}
			endpoint.publish(wsURL);
		} catch (Exception e) {
			endpoint = null;
			if (verbose) {
				System.out.printf("Caught exception when starting: %s%n", e);
				e.printStackTrace();
			}
			throw e;
		}
		publishToUDDI();
	}

	public void awaitConnections() {
		if (verbose) {
			System.out.println("Awaiting connections");
			System.out.println("Press enter to shutdown");
		}
		try {
			System.in.read();
		} catch (IOException e) {
			if (verbose) {
				System.out.printf("Caught i/o exception when awaiting requests: %s%n", e);
			}
		}
	}

	public void stop() throws Exception {
		try {
			if (endpoint != null) {
				// stop end point
				endpoint.stop();
				if (verbose) {
					System.out.printf("Stopped %s%n", wsURL);
				}
			}
		} catch (Exception e) {
			if (verbose) {
				System.out.printf("Caught exception when stopping: %s%n", e);
			}
		}

		this.portImpl = null;
		unpublishFromUDDI();
	}

	/* UDDI */

	void publishToUDDI() throws Exception {
		if(primary){
			try {
				// publish to UDDI
				if (uddiURL != null) {
					if (verbose) {
						System.out.printf("Publishing '%s' to UDDI at %s%n", wsName, uddiURL);
					}
					uddiNaming = new UDDINaming(uddiURL);
					uddiNaming.rebind(wsName, wsURL);
				}
			} catch (Exception e) {
				uddiNaming = null;
				if (verbose) {
					System.out.printf("Caught exception when binding to UDDI: %s%n", e);
				}
				throw e;
			}	

			System.out.println("I am the primary Mediator! I AM published!");
		}
		else {
			System.out.println("I am the secondary Mediator! I am NOT published!");
		}
	}

	void unpublishFromUDDI() {
		try {
			if (uddiNaming != null) {
				// delete from UDDI
				uddiNaming.unbind(wsName);
				if (verbose) {
					System.out.printf("Unpublished '%s' from UDDI%n", wsName);
				}
				uddiNaming = null;
			}
		} catch (Exception e) {
			if (verbose) {
				System.out.printf("Caught exception when unbinding: %s%n", e);
			}
		}
	}

}
