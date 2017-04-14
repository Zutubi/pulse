/* Copyright 2017 Zutubi Pty Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.zutubi.pulse.master.tove.config;

import com.zutubi.pulse.master.security.SecurityUtils;
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

    public boolean run(RecordManager recordManager)
    {
        return (Boolean) transactionContext.executeInsideTransaction(new NullaryFunction<Object>()
        {
            public Object process()
            {
                SecurityUtils.runAsSystem(new Runnable()
                {
                    public void run()
                    {
                        cleanupState();
                    }
                });
                return true;
            }
        });
    }

    public CleanupAction getCleanupAction()
    {
        return CleanupAction.NONE;
    }

    public abstract void cleanupState();
}
