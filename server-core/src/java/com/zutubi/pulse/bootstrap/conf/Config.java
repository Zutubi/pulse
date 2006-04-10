package com.cinnamonbob.bootstrap.conf;

/**
 * <class-comment/>
 */
public interface Config
{
    String getProperty(String key);

    void setProperty(String key, String value);

    boolean hasProperty(String key);

    void removeProperty(String key);
}
