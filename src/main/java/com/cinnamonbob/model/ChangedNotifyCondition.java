package com.cinnamonbob.model;

import com.cinnamonbob.core2.BuildResult;

/**
 * 
 *
 */
public class ChangedNotifyCondition
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
     * @see com.cinnamonbob.core.NotifyCondition#satisfied(com.cinnamonbob.core.BuildResult)
     */
    public boolean satisfied(BuildResult result)
    {
        return false;//result.changedBy(user.getLogin());
    }
}
