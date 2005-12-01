package com.cinnamonbob.model;

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
     * @see NotifyCondition#satisfied(com.cinnamonbob.core.model.RecipeResult)
     */
    public boolean satisfied(BuildResult result)
    {
        // look for a change.
        for (BuildScmDetails scmDetails : result.getScmDetails().values())
        {
            for (Changelist changelist : scmDetails.getChangelists())
            {
                if (changelist.getChanges().size() > 0)
                {
                    return true;
                }
            }
        }
        return false;
    }
}
