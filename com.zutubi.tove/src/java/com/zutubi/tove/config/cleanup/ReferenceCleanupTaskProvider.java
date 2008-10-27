package com.zutubi.tove.config.cleanup;

/**
 */
public interface ReferenceCleanupTaskProvider
{
    RecordCleanupTask getTask(String deletedPath, String referencingPath);
}
