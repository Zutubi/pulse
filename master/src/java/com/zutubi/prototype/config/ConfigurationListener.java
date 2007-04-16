package com.zutubi.prototype.config;

/**
 * A listener interface for handling configuration changes synchronously.
 * Consider instead handling config change events if closely-controlled
 * synchronous handling is not required.
 */
public interface ConfigurationListener
{
    void preInsert(String path);
    void postInsert(String path, String insertedPath, Object newInstance);
    void preSave(String path, Object oldInstance);
    void postSave(String path, Object oldInstance, String newPath, Object newInstance);
    void preDelete(String path, Object instance);
    void postDelete(String path, Object oldInstance);
}
