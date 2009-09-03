package com.zutubi.pulse.master.notifications.condition;

import com.zutubi.pulse.core.model.PersistentChangelist;
import com.zutubi.pulse.master.model.BuildManager;
import com.zutubi.pulse.master.model.BuildResult;
import com.zutubi.pulse.master.tove.config.user.UserConfiguration;
import com.zutubi.pulse.master.util.TransactionContext;
import com.zutubi.util.NullaryFunction;

import java.util.List;

/**
 * 
 *
 */
public class ChangedNotifyCondition implements NotifyCondition
{
    private TransactionContext transactionContext;
    private BuildManager buildManager;

    /**
     * Create a new condition
     */
    public ChangedNotifyCondition()
    {
    }

    public boolean satisfied(final BuildResult result, UserConfiguration user)
    {
        if(result == null)
        {
            return false;
        }

        // look for a change.
        return transactionContext.executeInsideTransaction(new NullaryFunction<Boolean>()
        {
            public Boolean process()
            {
                List<PersistentChangelist> changelists = buildManager.getChangesForBuild(result);
                for (PersistentChangelist changelist : changelists)
                {
                    if (changelist.getChanges() != null && changelist.getChanges().size() > 0)
                    {
                        return true;
                    }
                }

                return false;
            }
        });
    }

    public void setBuildManager(BuildManager buildManager)
    {
        this.buildManager = buildManager;
    }

    public void setTransactionContext(TransactionContext transactionContext)
    {
        this.transactionContext = transactionContext;
    }
}
