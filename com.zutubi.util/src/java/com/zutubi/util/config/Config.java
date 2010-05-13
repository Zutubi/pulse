package com.zutubi.util.config;

/**
 * The base interface for a configuration object that supports the getting and
 * setting of properties.
 */
public interface Config
{
    /**
     * Retrieve the named property.
     *
     * @param key uniquely identifies the property.
     *
     * @return the property value, or null if the property does not exist.
     */
    String getProperty(String key);

    /**
     * Set the value of the named property to specified value. If this key
     * already has a value, then it will be replaced by the new value.
     *
     * @param key uniquely identifies the property.
     *
     * @param value to be associated with the specified key.
     *
     * @see #isWritable()
     */
    void setProperty(String key, String value);

    /**
     * Returns true if this config contains the specified key.
     *
     * @param key uniquely identifies the property.
     *
     * @return true if the property exists, false otherwise.
     */
    boolean hasProperty(String key);

    /**
     * Remove the specified property from this config.
     *
     * @param key uniquely identifies the property.
     *
     * @see #isWritable()
     */
    void removeProperty(String key);

    /**
     * Indicates whether or not this configuration can be written to.
     *
     * @return true if setProperty and removeProperty can be called, false otherwise.
     */
    boolean isWritable();
}
