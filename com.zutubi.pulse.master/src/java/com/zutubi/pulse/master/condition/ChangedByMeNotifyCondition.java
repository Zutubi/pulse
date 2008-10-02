package com.zutubi.pulse.master.condition;

import com.zutubi.pulse.core.model.Changelist;
import com.zutubi.pulse.model.BuildManager;
import com.zutubi.pulse.model.BuildResult;
import com.zutubi.pulse.tove.config.user.UserConfiguration;

import java.util.List;

/**
 * 
 *
 */
public class ChangedByMeNotifyCondition implements NotifyCondition
{
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
        final boolean[] response = new boolean[1];
        buildManager.executeInTransaction(new Runnable()
        {
            public void run()
            {
                List<Changelist> changelists = buildManager.getChangesForBuild(result);
                for (Changelist changelist : changelists)
                {
                    if (byMe(changelist, user) && changelist.getChanges() != null && changelist.getChanges().size() > 0)
                    {
                        response[0] = true;
                        return;
                    }
                }
                response[0] = false;
            }
        });

        return response[0];
    }

    private boolean byMe(Changelist changelist, UserConfiguration user)
    {
        String author = changelist.getRevision().getAuthor();
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
}
