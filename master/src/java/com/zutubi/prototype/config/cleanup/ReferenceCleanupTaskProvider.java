package com.zutubi.prototype.config.cleanup;

/**
 */
public interface ReferenceCleanupTaskProvider
{
    RecordCleanupTask getAction(String deletedPath, String referencingPath);
}
