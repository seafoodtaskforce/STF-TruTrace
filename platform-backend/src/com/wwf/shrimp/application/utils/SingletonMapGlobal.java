package com.wwf.shrimp.application.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class SingletonMapGlobal {
    private static final SingletonMapGlobal instance = new SingletonMapGlobal();
    private ConcurrentHashMap sessionMap = null;
    private InetAddress ip;
    private String hostname;
    private String externalIP;

    private SingletonMapGlobal() {
        sessionMap = new ConcurrentHashMap();
        
        try {
            ip = InetAddress.getLocalHost();
            hostname = ip.getHostName();
            System.out.println("Your current IP address : " + ip);
            System.out.println("Your current Hostname : " + hostname);
            
            URL whatismyip = new URL("http://checkip.amazonaws.com");
            BufferedReader in = new BufferedReader(new InputStreamReader(
                            whatismyip.openStream()));

            externalIP = in.readLine(); //you get the IP as a String
            System.out.println(externalIP);
 
        } catch (UnknownHostException e) {
 
            e.printStackTrace();
        } catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
    }

    public static synchronized SingletonMapGlobal getInstance() {
        return instance;
    }
    
    public void setValue(Object key, Object value) {
    	sessionMap.put(key, value);
    }
    
    public Object getValue(Object key) {
    	return sessionMap.get(key);
    }
    
    public void addDiagnostic(String key, String value) {
    	if(sessionMap.get(key) == null){
    		List<String> diagnostics = new ArrayList<String>();
    		sessionMap.put(key, diagnostics);
    	}
    	//
    	// append a new value
    	List<String> diagnostics = (List<String>)sessionMap.get(key);
    	diagnostics.add(value);
    	sessionMap.put(key, diagnostics);
    }
    
    public List<String> getDiagnostics(Object key) {
    	return (List<String>)sessionMap.get(key);
    }
    
    public void clearDiagnostics(String key) {
    	sessionMap.remove(key);
    }
    
    public String getIPAddress() {
    	return ip.toString();
    }
    
    public String getIPAddressHost() {
    	return hostname;
    }
    
    public String getExternalIP() {
    	return externalIP;
    }
    
    public String getDiagnosticKey(){
    	return "Diagnostic Key";
    }

}
