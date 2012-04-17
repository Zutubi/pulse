package com.zutubi.pulse.master.notifications.condition;

import com.zutubi.pulse.master.model.BuildResult;
import com.zutubi.pulse.master.model.ChangelistManager;
import com.zutubi.pulse.master.tove.config.user.UserConfiguration;
import com.zutubi.pulse.master.util.TransactionContext;
import com.zutubi.util.NullaryFunction;

/**
 * 
 *
 */
public class ChangedNotifyCondition implements NotifyCondition
{
    private TransactionContext transactionContext;
    private ChangelistManager changelistManager;

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
                return changelistManager.getChangesForBuild(result, 0, false).size() > 0;
            }
        });
    }

    public void setChangelistManager(ChangelistManager changelistManager)
    {
        this.changelistManager = changelistManager;
    }

    public void setTransactionContext(TransactionContext transactionContext)
    {
        this.transactionContext = transactionContext;
    }
}
