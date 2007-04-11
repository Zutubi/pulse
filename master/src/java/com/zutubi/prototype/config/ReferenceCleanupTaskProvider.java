package com.zutubi.prototype.config;

/**
 */
public interface ReferenceCleanupTaskProvider
{
    ReferenceCleanupTask getAction(String deletedPath, String referencingPath);
}
