package com.zutubi.prototype.config;

import java.util.Collection;

/**
 */
public interface ConfigurationProvider
{
    <T> T get(String path, Class<T> clazz);
    <T> T get(Class<T> clazz);
    <T> Collection<T> getAll(Class<T> clazz);

    void registerListener(String path, ConfigurationListener listener);
    void registerListener(Class clazz, ConfigurationListener listener);
    void unregisterListener(ConfigurationListener listener);

    void registerEventListener(ConfigurationEventListener listener, String... paths);
    void registerEventListener(ConfigurationEventListener listener, Class clazz);
    void unregisterEventListener(ConfigurationEventListener listener);
}
