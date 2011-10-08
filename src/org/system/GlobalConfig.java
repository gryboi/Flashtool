package org.system;

public class GlobalConfig {
	private static PropertiesFile config;
	
	public static String getProperty (String property) {
		if (config==null) {
		config = new PropertiesFile("gui/ressources/config.properties","./config.properties");
		config.write("UTF-8");
		}
		return config.getProperty(property);
	}

}
