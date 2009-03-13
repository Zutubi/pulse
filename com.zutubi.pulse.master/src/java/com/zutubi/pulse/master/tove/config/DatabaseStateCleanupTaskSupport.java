package com.zutubi.pulse.master.tove.config;

import com.zutubi.pulse.master.security.AcegiUtils;
import com.zutubi.pulse.master.util.TransactionContext;
import com.zutubi.tove.config.cleanup.RecordCleanupTaskSupport;
import com.zutubi.tove.type.record.RecordManager;
import com.zutubi.util.NullaryFunction;

/**
 * Helper base for cleanup tasks that remove state from the DB.  Ensures all
 * DB interaction goes into a single transaction so that if an exception is
 * thrown it is all rolled back (as the config cleanup will also be).
 */
public abstract class DatabaseStateCleanupTaskSupport extends RecordCleanupTaskSupport
{
    private TransactionContext transactionContext;

    public DatabaseStateCleanupTaskSupport(String path, TransactionContext transactionContext)
    {
        super(path);
        this.transactionContext = transactionContext;
    }

    public void run(RecordManager recordManager)
    {
        transactionContext.executeInsideTransaction(new NullaryFunction<Object>()
        {
            public Object process()
            {
                AcegiUtils.runAsSystem(new Runnable()
                {
                    public void run()
                    {
                        cleanupState();
                    }
                });
                return null;
            }
        });
    }

    public abstract void cleanupState();
}
