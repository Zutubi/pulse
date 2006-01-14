package com.cinnamonbob.model;

import com.cinnamonbob.core.model.Changelist;

/**
 * 
 *
 */
public class ChangedNotifyCondition implements NotifyCondition
{

    /**
     * Create a new condition
     */
    public ChangedNotifyCondition()
    {
    }

    /**
     * @see NotifyCondition#satisfied(com.cinnamonbob.model.BuildResult)
     */
    public boolean satisfied(BuildResult result)
    {
        // look for a change.
        BuildScmDetails scmDetails = result.getScmDetails();
        for (Changelist changelist : scmDetails.getChangelists())
        {
            if (changelist.getChanges().size() > 0)
            {
                return true;
            }
        }
        return false;
    }
}
