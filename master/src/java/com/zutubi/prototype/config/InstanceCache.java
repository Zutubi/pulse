package com.zutubi.prototype.config;

import java.util.Collection;

/**
 */
public interface InstanceCache
{
    Object get(String path);

    Collection<Object> getAllDescendents(String path);

    void getAllMatchingPathPattern(String path, Collection result);

    void put(String path, Object instance);

    void clear();
}
