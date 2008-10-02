package com.zutubi.pulse.tove.config;

import com.zutubi.pulse.master.model.BuildManager;
import com.zutubi.tove.config.cleanup.RecordCleanupTaskSupport;

/**
 * Helper base for cleanup tasks that remove state from the DB.  Ensures all
 * DB interaction goes into a single transaction so that if an exception is
 * thrown it is all rolled back (as the config cleanup will also be).
 */
public abstract class DatabaseStateCleanupTaskSupport extends RecordCleanupTaskSupport
{
    private BuildManager buildManager;

    public DatabaseStateCleanupTaskSupport(String path, BuildManager buildManager)
    {
        super(path);
        this.buildManager = buildManager;
    }

    public void run()
    {
        buildManager.executeInTransaction(new Runnable()
        {
            public void run()
            {
                cleanupState();
            }
        });
    }

    public abstract void cleanupState();
}
