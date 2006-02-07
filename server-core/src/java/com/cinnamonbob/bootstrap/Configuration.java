package com.cinnamonbob.bootstrap;

/**
 * <class-comment/>
 */
public interface Configuration
{
    /**
     * Set the property.
     *
     * @param key
     * @param value
     *
     * @throws UnsupportedOperationException is this configuration is read only.
     */
    void setProperty(String key, String value);

    String getProperty(String key);

    boolean hasProperty(String key);

    void resetDefaults();
}
