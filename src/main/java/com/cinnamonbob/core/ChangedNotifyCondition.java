package com.cinnamonbob.core;

/**
 * A notify condition satisfied when a particular builder is deemed to have
 * changed something influencing the build.
 * 
 * @author jsankey
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
     * @see com.cinnamonbob.core.NotifyCondition#satisfied(com.cinnamonbob.core.BuildResult)
     */
    public boolean satisfied(BuildResult result)
    {
        return result.changedBy(user.getLogin());
    }

}
