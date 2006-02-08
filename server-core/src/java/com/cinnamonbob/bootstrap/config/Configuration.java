package com.cinnamonbob.bootstrap.config;

/**
 * <class-comment/>
 */
public interface Configuration
{
    String getProperty(String key);

    void setProperty(String key, String value);

    boolean hasProperty(String key);

    void removeProperty(String key);
}
