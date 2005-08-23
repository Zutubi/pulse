package com.cinnamonbob.model;

/**
 * 
 *
 */
public class ChangedNotifyCondition implements NotifyCondition
{

    /**
     * Create a new condition
     * 
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
        for (Changelist changelist : result.getChangelists())
        {
            if (changelist.getChanges().size() > 0)
            {
                return true;
            }
        }
        return false;
    }
}
