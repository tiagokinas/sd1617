package org.komparator.security;


public class KeystoreAliasManager{
    private String wsName = "deafult";
    
    private static class SingletonHolder {
        private static final KeystoreAliasManager INSTANCE = new KeystoreAliasManager();
    }
    
    public static KeystoreAliasManager getInstance() {
        return SingletonHolder.INSTANCE;
    }
        
    private KeystoreAliasManager(){}
    
    public String getWsName(){return wsName;}

    public String getKeyAlias(){return wsName.toLowerCase();}

    public String getKeyStoreName(){return wsName + ".jks";}

    public String getCertificateName(){ return wsName + ".cer";}

    public String getKeyStorePath(){ return "src\\main\\resources\\" + getKeyStoreName();}
    
    public void setWsName(String name){wsName=name;}
}
