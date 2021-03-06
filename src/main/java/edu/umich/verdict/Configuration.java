package edu.umich.verdict;

import java.io.*;
import java.util.HashMap;
import java.util.Properties;
import java.util.Scanner;

public class Configuration {
    private HashMap<String, String> configs = new HashMap<>();

    public Configuration() {
        setDefaults();
    }

    public Configuration(Properties properties) {
        this();
        for (String prop : properties.stringPropertyNames())
            this.set(prop, properties.getProperty(prop));
    }

    public Configuration(File file) throws FileNotFoundException {
        this();
        updateFromStream(new FileInputStream(file));
    }

    private Configuration setDefaults() {
        try {
            ClassLoader cl = this.getClass().getClassLoader();
            updateFromStream(cl.getResourceAsStream("default.conf"));
        } catch (FileNotFoundException e) {
            System.err.println(e.getMessage());
        }
        return this;
    }

    private Configuration updateFromStream(InputStream stream) throws FileNotFoundException {
        Scanner scanner = new Scanner(stream);
        while (scanner.hasNext()) {
            String line = scanner.nextLine();
            if (!line.isEmpty() && !line.startsWith("#"))
                set(line);
        }
        return this;
    }

    public int getInt(String key) {
        return Integer.parseInt(get(key));
    }

    public boolean getBoolean(String key) {
        String val = get(key);
        if (val == null)
            return false;
        val = val.toLowerCase();
        return val.equals("on") || val.equals("yes") || val.equals("true") || val.equals("1");
    }

    public double getDouble(String key) {
        return Double.parseDouble(get(key));
    }

    public double getPercent(String key) {
        String val = get(key);
        if (val.endsWith("%"))
            return Double.parseDouble(val.substring(0, val.length() - 1)) / 100;
        return Double.parseDouble(val);
    }

    public String get(String key) {
        return configs.getOrDefault(key.toLowerCase(), configs.getOrDefault(configs.getOrDefault("dbms", "") + "." + key
                .toLowerCase(), null));
    }

    private Configuration set(String keyVal) {
        int equalIndex = keyVal.indexOf('=');
        if (equalIndex == -1)
            return this;
        String key = keyVal.substring(0, equalIndex).trim();
        String val = keyVal.substring(equalIndex + 1).trim();
        if (val.startsWith("\"") && val.endsWith("\""))
            val = val.substring(1, val.length() - 1);
        return set(key, val);
    }

    public Configuration set(String key, String value) {
        configs.put(key.toLowerCase(), value);
        return this;
    }

    public Properties toProperties() {
        Properties p = new Properties();
        for (String key : configs.keySet()) {
            p.setProperty(key, configs.get(key));
        }
        return p;
    }
}
