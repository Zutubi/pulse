package com.zutubi.pulse.condition;

import com.zutubi.pulse.core.model.Changelist;
import com.zutubi.pulse.model.BuildManager;
import com.zutubi.pulse.model.BuildResult;
import com.zutubi.pulse.model.User;

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

    public boolean satisfied(BuildResult result, User user)
    {
        // look for a change.
        List<Changelist> changelists = buildManager.getChangesForBuild(result);
        for (Changelist changelist : changelists)
        {
            if (byMe(changelist, user) && changelist.getChanges() != null && changelist.getChanges().size() > 0)
            {
                return true;
            }
        }

        return false;
    }

    private boolean byMe(Changelist changelist, User user)
    {
        String author = changelist.getRevision().getAuthor();
        if(author.equals(user.getLogin()))
        {
            return true;
        }

        for(String alias: user.getAliases())
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
