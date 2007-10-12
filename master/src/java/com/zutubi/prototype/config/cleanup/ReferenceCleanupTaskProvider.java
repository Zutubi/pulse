package com.zutubi.prototype.config.cleanup;

/**
 */
public interface ReferenceCleanupTaskProvider
{
    RecordCleanupTask getTask(String deletedPath, String referencingPath);
}
