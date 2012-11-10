package config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class Configuration {
	private static Configuration instance;
	private String defaultServer;
	private int defaultPort;
	private String adminPassword;
	private Properties properties;
	
	private Configuration(){
		this.properties = new Properties();
		try {
			InputStream is=Configuration.class.getResourceAsStream("general.properties");
		    properties.load(is);
		} catch (IOException ex) {
		    ex.printStackTrace();
		}
		defaultServer=properties.getProperty("defaultServer", "localhost");
		defaultPort=Integer.valueOf(properties.getProperty("defaultPort", "110"));
		adminPassword=properties.getProperty("adminPassword", "admin");
	}
	
	public static Configuration getInstance(){
		if(instance==null){
			instance=new Configuration();
		}
		return instance;
	}
	
	public String getDefaultServer(){
		return defaultServer;
	}
	
	public int getDefaultPort(){
		return defaultPort;
	}
	
	public String getAdminPassword(){
		return adminPassword;
	}
}
