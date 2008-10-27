package com.zutubi.tove.config;

/**
 */
public interface InstanceSource
{
    Configuration get(String path, boolean allowIncomplete);
}
