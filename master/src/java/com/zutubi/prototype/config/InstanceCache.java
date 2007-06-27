package com.zutubi.prototype.config;

import java.util.Collection;

/**
 */
public interface InstanceCache
{
    Object get(String path);

    void getAll(String path, Collection result);

    void put(String path, Object instance);

    void clear();
}
