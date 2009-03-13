package com.zutubi.pulse.master.condition;

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
                List<PersistentChangelist> changelists = buildManager.getChangesForBuild(result);
                for (PersistentChangelist changelist : changelists)
                {
                    if (byMe(changelist, user) && changelist.getChanges() != null && changelist.getChanges().size() > 0)
                    {
                        return true;
                    }
                }
                return false;
            }
        });
    }

    private boolean byMe(PersistentChangelist changelist, UserConfiguration user)
    {
        String author = changelist.getAuthor();
        if(author.equals(user.getLogin()))
        {
            return true;
        }

        for(String alias: user.getPreferences().getAliases())
        {
            if(author.equals(alias))
            {
                return true;
            }
        }

        return false;
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
