package org.komparator.mediator.ws;

import java.lang.Thread;
import java.util.Date;

import org.komparator.mediator.ws.cli.MediatorClient;
import org.komparator.mediator.ws.*;

import com.sun.xml.ws.client.ClientTransportException;

public class LifeProof extends Thread {

    Object result;
    Exception exception;
    Object argument;
    Object arg2 = null;

    public LifeProof(Object argument, Object arg2) {
        this.argument = argument;
        this.arg2 = arg2;
    }

    public Object getResult() {
        synchronized(this) {
            return this.result;
        }
    }

    public Exception getException() {
        synchronized(this) {
            return this.exception;
        }
    }

    public void run() {
        try {
            System.out.println(this.getClass() + " running...");
            Integer arg = (Integer) this.argument;

			if(arg2 instanceof MediatorEndpointManager){
				boolean published = true;
				while(published){
					if(arg2 != null && ((MediatorEndpointManager)arg2).hasTimestamp() && (new Date().getTime() - ((MediatorEndpointManager)arg2).getTimestamp().getTime() > 4900)) {
						((MediatorEndpointManager) arg2).setPrimary();
						((MediatorEndpointManager) arg2).publishToUDDI();
						System.out.println("UNLIMITED POWER!");
						published = false;
					}
					System.out.println("LifeProof is sleeping...");
					sleep(2000);
				}
            }
            else if(arg2 instanceof String){
            	while(true){
	        		MediatorClient mc = new MediatorClient((String)arg2);
	            	mc.imAlive();

					System.out.println("LifeProof is sleeping...");
	            	sleep(arg*1000);
				}

            }
            else{
            	System.err.println("LifeProof is doing nothing!");
            }

 			synchronized(this) {
                this.notifyAll();
            }
			
			this.result = new Integer(arg + 1);
        } catch(ClientTransportException cte){
        	System.err.println("No backup server is available. Simon is lonely....");
        } catch (Exception e) {
            System.out.println(this.getClass() + " caught exception: " + e.toString());
             e.printStackTrace(System.out);
            this.exception = e;

        } finally {
            System.out.println(this.getClass() + " stopping.");
        }
    }

}
