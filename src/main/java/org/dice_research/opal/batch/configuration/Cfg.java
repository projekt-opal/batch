package org.dice_research.opal.batch.configuration;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

/**
 * Configuration based on property files.
 *
 * @author Adrian Wilke
 */
public class Cfg {

	private static final String DEFAULT_CONFIGURATION_FILE = "default.properties";
	private Properties properties;

	public Cfg() {
		this(new File(DEFAULT_CONFIGURATION_FILE));
	}

	public Cfg(File file) {
		properties = new Properties();
		try {
			properties.load(new FileReader(file));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public String get(String key) {
		return properties.getProperty(key);
	}

	public boolean getBoolean(String key) {
		return Boolean.parseBoolean(properties.getProperty(key));
	}

	public boolean has(String key) {
		return properties.containsKey(key);
	}
}