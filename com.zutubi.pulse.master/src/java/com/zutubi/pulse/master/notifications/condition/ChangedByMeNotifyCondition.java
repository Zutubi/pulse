package com.zutubi.pulse.master.notifications.condition;

import com.zutubi.pulse.core.model.PersistentChangelist;
import com.zutubi.pulse.master.model.BuildManager;
import com.zutubi.pulse.master.model.BuildResult;
import com.zutubi.pulse.master.tove.config.user.UserConfiguration;
import com.zutubi.pulse.master.util.TransactionContext;
import com.zutubi.util.CollectionUtils;
import com.zutubi.util.NullaryFunction;

import java.util.List;

/**
 * A condition that is true if the build contains changes by the user.
 */
public class ChangedByMeNotifyCondition implements NotifyCondition
{
    private TransactionContext transactionContext;
    private BuildManager buildManager;

    /**
     * Create a new condition
     */
    public ChangedByMeNotifyCondition()
    {
    }

    public boolean satisfied(final BuildResult result, final UserConfiguration user)
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
                List<PersistentChangelist> changelists = buildManager.getChangesForBuild(result, 0, false);
                return CollectionUtils.contains(changelists, new ByMePredicate(user));
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
