/********************************************************************************
 @COPYRIGHT@
 ********************************************************************************/
package com.zutubi.pulse.condition;

import com.zutubi.pulse.core.model.Changelist;
import com.zutubi.pulse.condition.NotifyCondition;
import com.zutubi.pulse.model.BuildResult;
import com.zutubi.pulse.model.User;
import com.zutubi.pulse.model.BuildScmDetails;

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

    public boolean satisfied(BuildResult result, User user)
    {
        // look for a change.
        BuildScmDetails scmDetails = result.getScmDetails();
        if (scmDetails != null && scmDetails.getChangelists() != null)
        {
            for (Changelist changelist : scmDetails.getChangelists())
            {
                if (changelist.getChanges() != null && changelist.getChanges().size() > 0)
                {
                    return true;
                }
            }
        }

        return false;
    }
}
