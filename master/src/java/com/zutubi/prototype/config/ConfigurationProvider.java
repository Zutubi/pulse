package com.zutubi.prototype.config;

import com.zutubi.pulse.prototype.config.ProjectConfiguration;
import com.zutubi.pulse.core.config.Resource;
import com.zutubi.pulse.core.config.Configuration;

import java.util.Collection;
import java.util.List;

/**
 */
public interface ConfigurationProvider
{
    void init();
    
    <T> T get(String path, Class<T> clazz);
    <T> T get(Class<T> clazz);
    <T> Collection<T> getAll(String path, Class<T> clazz);
    <T> Collection<T> getAll(Class<T> clazz);

    String insert(String path, Object instance);
    void save(String parentPath, String baseName, Object instance);
    void delete(String path);

    void registerEventListener(ConfigurationEventListener listener, boolean synchronous, String... paths);
    void registerEventListener(ConfigurationEventListener listener, boolean synchronous, Class clazz);
    void unregisterEventListener(ConfigurationEventListener listener);

}
