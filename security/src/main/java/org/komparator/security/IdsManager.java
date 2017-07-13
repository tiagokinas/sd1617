package org.komparator.security;

import java.lang.*;
import java.util.*;
import javax.xml.soap.*;
import javax.xml.ws.handler.*;
import javax.xml.soap.SOAPMessage;

public class IdsManager{
    
    private Map<String, SOAPMessage> ids = new TreeMap<>();

    private static class SingletonHolder {
        private static final IdsManager INSTANCE = new IdsManager();
    }
    
    public static IdsManager getInstance() {
        return SingletonHolder.INSTANCE;
    }
        
    private IdsManager(){}
    
    public SOAPMessage getMessage(String id){return ids.get(id);}
    
    public void addMessage(String id, SOAPMessage smc){ids.put(id,smc);}

    public boolean existsMessage(String id){return ids.containsKey(id);}

    public String getLowerKey() throws Exception {
        for(Map.Entry<String, SOAPMessage> entry : ids.entrySet()){
            if(entry.getValue()==null){
                return entry.getKey();
            }
        }
        throw new Exception("No key where value is empty!");

    }

    public Set<String> getKeys(){return ids.keySet();}
}
