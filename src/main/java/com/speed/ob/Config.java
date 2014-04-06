package com.speed.ob;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * See LICENSE.txt for license info
 */
public class Config {

    private Properties properties;

    private static final Properties DEFAULT_CONFIG;

    static {
        DEFAULT_CONFIG = new Properties() {
            {
                setProperty("Obfuscator.logging", "info");
                setProperty("Obfuscator.log_dir", "logs");
                setProperty("Obfuscator.out_dir", System.getProperty("user.home"));
                setProperty("Obfuscator.input", "example.jar");
                setProperty("ClassNameTransform.exclude_mains", "true");
                setProperty("ClassNameTransform.excludes", "");
                setProperty("ClassNameTransform.keep_packages", "false");
                setProperty("Obfuscator.all_transforms", "false");
                setProperty("Obfuscator.classname_obfuscation", "false");
                setProperty("Obfuscator.controlflow_obfuscation", "false");
                setProperty("Obfuscator.string_obfuscation", "false");
                setProperty("Obfuscator.fieldname_transforms", "false");
                setProperty("Obfuscator.methodname_transforms", "false");
            }
        };
     /*   try {
            FileOutputStream out = new FileOutputStream("default.properties");
            DEFAULT_CONFIG.store(out, "Default configuration for Ob2");
            out.close();

        } catch (IOException e) {
            e.printStackTrace();
        }    */
    }

    public Config(String fileName) throws IOException {
        Properties props = new Properties();
        props.putAll(DEFAULT_CONFIG);
        File file = new File(fileName);
        if (!file.exists() || file.isDirectory()) {
            throw new IOException("File could not be found " + file.getName());
        }
        FileInputStream fis = new FileInputStream(file);
        if (file.getName().endsWith(".xml")) {
            props.loadFromXML(fis);
        } else {
            props.load(fis);
        }
        properties.putAll(props);
    }

    public Config() {
        properties = new Properties();
        properties.putAll(DEFAULT_CONFIG);
    }

    /**
     * Will overwrite any values current config with those from supplied config
     *
     * @param config the config to merge with this config
     */
    public void merge(final Config config) {
        properties.putAll(config.properties);
    }

    public int getInt(final String key) throws IllegalArgumentException {
        String value = get(key);
        if (value == null) {
            throw new IllegalArgumentException("This key was not found in the properties: " + key);
        }
        return Integer.parseInt(value);
    }

    public String get(final String key) {
        return properties.get(key).toString();
    }

    public boolean getBoolean(final String key) {
        String value = get(key);
        if (value == null) {
            throw new IllegalArgumentException("This key was not found in the properties: " + key);
        }
        return value.equalsIgnoreCase("yes") || value.equalsIgnoreCase("true");
    }

    public List<String> getList(String key) {
        List<String> list = new ArrayList<>();
        String value = get(key);
        if (value == null) return null;
        if (value.isEmpty()) return list;
        String[] parts = value.split(",");
        for (int i = 0; i < parts.length; i++) {
            list.add(parts[i]);
        }
        return list;
    }

    public long getLong(final String key) throws IllegalArgumentException {
        String value = get(key);
        if (value == null) {
            throw new IllegalArgumentException("This key was not found in the properties: " + key);
        }
        return Long.parseLong(value);
    }

    public double getDouble(final String key) throws IllegalArgumentException {
        String value = get(key);
        if (value == null) {
            throw new IllegalArgumentException("This key was not found in the properties: " + key);
        }
        return Double.parseDouble(value);
    }

    public void set(String key, String value) {
        properties.setProperty(key, value);

    }
}
