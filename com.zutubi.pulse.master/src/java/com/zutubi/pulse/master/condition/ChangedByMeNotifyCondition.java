package com.zutubi.pulse.master.condition;

import com.zutubi.pulse.core.model.PersistentChangelist;
import com.zutubi.pulse.master.model.BuildManager;
import com.zutubi.pulse.master.model.BuildResult;
import com.zutubi.pulse.master.tove.config.user.UserConfiguration;
import com.zutubi.util.CollectionUtils;

import java.util.List;

/**
 * A condition that is true if the build contains changes by the user.
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
                List<PersistentChangelist> changelists = buildManager.getChangesForBuild(result);
                response[0] = CollectionUtils.contains(changelists, new ByMePredicate(user));
            }
        });

        return response[0];
    }

    public void setBuildManager(BuildManager buildManager)
    {
        this.buildManager = buildManager;
    }
}
