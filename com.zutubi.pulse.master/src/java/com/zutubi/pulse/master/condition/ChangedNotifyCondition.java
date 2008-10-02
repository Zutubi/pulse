package com.zutubi.pulse.master.condition;

import com.zutubi.pulse.core.model.Changelist;
import com.zutubi.pulse.master.model.BuildManager;
import com.zutubi.pulse.master.model.BuildResult;
import com.zutubi.pulse.tove.config.user.UserConfiguration;

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

    public boolean satisfied(final BuildResult result, UserConfiguration user)
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
                    if (changelist.getChanges() != null && changelist.getChanges().size() > 0)
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

    public void setBuildManager(BuildManager buildManager)
    {
        this.buildManager = buildManager;
    }
}
