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
public class ChangedNotifyCondition implements NotifyCondition
{
    private BuildManager buildManager;

    /**
     * Create a new condition
     */
    public ChangedNotifyCondition()
    {
    }

    public boolean satisfied(BuildResult result, User user)
    {
        // look for a change.
        List<Changelist> changelists = buildManager.getChangesForBuild(result);
        for (Changelist changelist : changelists)
        {
            if (changelist.getChanges() != null && changelist.getChanges().size() > 0)
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
