package com.zutubi.prototype.config;

/**
 */
public interface ReferenceCleanupTaskProvider
{
    RecordCleanupTask getAction(String deletedPath, String referencingPath);
}
