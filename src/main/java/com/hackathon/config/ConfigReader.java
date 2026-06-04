package com.hackathon.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public final class ConfigReader {

    private static final String FILE = "config.properties";
    private static ConfigReader instance;
    private final Properties props = new Properties();

    // Loads config.properties from the classpath; throws if missing or unreadable.
    private ConfigReader() {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        if (cl == null)
            cl = getClass().getClassLoader();
        try (InputStream is = cl.getResourceAsStream(FILE)) {
            if (is == null)
                throw new IllegalStateException(FILE + " not found on classpath");
            props.load(is);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load " + FILE, e);
        }
    }

    // Returns the singleton ConfigReader, creating it on first call.
    public static synchronized ConfigReader get() {
        if (instance == null)
            instance = new ConfigReader();
        return instance;
    }

    // Returns the value for key; system properties override the file; throws if
    // absent.
    public String get(String key) {
        String sys = System.getProperty(key);
        if (sys != null && !sys.isBlank())
            return sys.trim();
        String val = props.getProperty(key);
        if (val == null)
            throw new IllegalArgumentException("Missing config key: " + key);
        return val.trim();
    }

    // Returns the value for key, or the supplied default if the key is absent.
    public String get(String key, String def) {
        try {
            return get(key);
        } catch (IllegalArgumentException e) {
            return def;
        }
    }

    // Returns the value for key parsed as int.
    public int getInt(String key) {
        return Integer.parseInt(get(key));
    }

    // Returns the value for key parsed as double.
    public double getDouble(String key) {
        return Double.parseDouble(get(key));
    }

    // Returns the value for key parsed as boolean (true/false, case-insensitive).
    public boolean getBoolean(String key) {
        return Boolean.parseBoolean(get(key));
    }
}
