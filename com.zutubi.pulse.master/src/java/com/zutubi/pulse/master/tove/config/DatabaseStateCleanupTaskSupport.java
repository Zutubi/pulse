package com.zutubi.pulse.master.tove.config;

import com.zutubi.pulse.master.model.BuildManager;
import com.zutubi.pulse.master.security.AcegiUtils;
import com.zutubi.tove.config.cleanup.RecordCleanupTaskSupport;
import com.zutubi.tove.type.record.RecordManager;

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

    public void run(RecordManager recordManager)
    {
        buildManager.executeInTransaction(new Runnable()
        {
            public void run()
            {
                AcegiUtils.runAsSystem(new Runnable()
                {
                    public void run()
                    {
                        cleanupState();
                    }
                });
            }
        });
    }

    public abstract void cleanupState();
}
