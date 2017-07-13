package org.komparator.supplier.ws;

import java.io.IOException;

import javax.xml.ws.Endpoint;
import pt.ulisboa.tecnico.sdis.ws.uddi.UDDINaming;


/** End point manager */
public class SupplierEndpointManager {

	/** Web Service location to publish */
	private String url = null;
	private String uddiURL = null;
	private String name = null;

	/** Port implementation */
	private SupplierPortImpl portImpl = new SupplierPortImpl(this);

// TODO
//	/** Obtain Port implementation */
//	public SupplierPortType getPort() {
//		return portImpl;
//	}

	/** Web Service end point */
	private Endpoint endpoint = null;
	private UDDINaming uddiNaming = null;

	/** output option **/
	private boolean verbose = true;

	public boolean isVerbose() {
		return verbose;
	}

	public void setVerbose(boolean verbose) {
		this.verbose = verbose;
	}
	
	public String getWsName(){
		return name;
	}

	/** constructor with provided web service URL */
	public SupplierEndpointManager(String _uddiURL, String _name, String _url) {
		if (_url == null)
			throw new NullPointerException("Web Service URL cannot be null!");
		this.url = _url;
		this.uddiURL = _uddiURL;
		this.name = _name;
	}

	/* end point management */

	public void start() throws Exception {

		try {
			endpoint = Endpoint.create(this.portImpl);

			// publish endpoint
			System.out.printf("Starting %s%n", url);
			endpoint.publish(url);

			// publish to UDDI
			System.out.printf("Publishing '%s' to UDDI at %s%n", name, uddiURL);
			uddiNaming = new UDDINaming(uddiURL);
			uddiNaming.rebind(name, url);

		} catch (Exception e) {
			System.out.printf("Caught exception: %s%n", e);
			e.printStackTrace();

		}
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
					System.out.printf("Stopped %s%n", url);
				}
			}
		} catch (Exception e) {
			if (verbose) {
				System.out.printf("Caught exception when stopping: %s%n", e);
			}
		}
		try {
			if (uddiNaming != null) {
                // delete from UDDI
				uddiNaming.unbind(name);
				System.out.printf("Deleted '%s' from UDDI%n", name);
			}
		} catch (Exception e) {
			System.out.printf("Caught exception when deleting: %s%n", e);
		}
		this.portImpl = null;
	}

}
