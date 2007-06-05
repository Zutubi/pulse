package com.zutubi.prototype.config;

import com.zutubi.pulse.core.config.Configuration;

import java.util.Collection;

/**
 */
public interface ConfigurationProvider
{
    void init();
    
    <T> T get(String path, Class<T> clazz);
    <T> T get(Class<T> clazz);
    <T> Collection<T> getAll(String path, Class<T> clazz);
    <T> Collection<T> getAll(Class<T> clazz);

    <T> T getAncestorOfType(Configuration c, Class<T> clazz);

    String insert(String path, Object instance);
    void save(String parentPath, String baseName, Object instance);
    void delete(String path);

    void registerEventListener(ConfigurationEventListener listener, boolean synchronous, boolean includeChildPaths, String... paths);
    void registerEventListener(ConfigurationEventListener listener, boolean synchronous, boolean includeChildPaths, Class clazz);

    /**
     * Register an event listener to be triggered whenever an instance of the specified class is the subject of the
     * event.
     *
     * @param listener to be triggered.
     * @param synchronous
     * @param clazz
     */
    void registerEventListener(ConfigurationEventListener listener, boolean synchronous, Class clazz);

    void unregisterEventListener(ConfigurationEventListener listener);
}
