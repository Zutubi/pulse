package com.zutubi.prototype.config;

import java.util.Collection;

/**
 */
public interface ConfigurationProvider
{
    void init();
    
    <T> T get(String path, Class<T> clazz);
    <T> T get(Class<T> clazz);
    <T> Collection<T> getAll(Class<T> clazz);

    void registerEventListener(ConfigurationEventListener listener, boolean synchronous, String... paths);
    void registerEventListener(ConfigurationEventListener listener, boolean synchronous, Class clazz);
    void unregisterEventListener(ConfigurationEventListener listener);
}
