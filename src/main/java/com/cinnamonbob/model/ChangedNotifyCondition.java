package com.cinnamonbob.model;

/**
 * 
 *
 */
public class ChangedNotifyCondition implements NotifyCondition
{
    /**
     * The user to test for changes by.
     */
    private User user;
    
    /**
     * Create a new condition based on the given user.
     * 
     * @param user
     *        the user to test for changes by
     */
    public ChangedNotifyCondition(User user)
    {
        this.user = user;
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
