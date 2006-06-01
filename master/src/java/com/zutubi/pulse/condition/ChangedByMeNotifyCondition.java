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
public class ChangedByMeNotifyCondition implements NotifyCondition
{
    /**
     * Create a new condition
     */
    public ChangedByMeNotifyCondition()
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
                if (byMe(changelist, user) && changelist.getChanges() != null && changelist.getChanges().size() > 0)
                {
                    return true;
                }
            }
        }

        return false;
    }

    private boolean byMe(Changelist changelist, User user)
    {
        String author = changelist.getRevision().getAuthor();
        if(author.equals(user.getLogin()))
        {
            return true;
        }

        for(String alias: user.getAliases())
        {
            if(author.equals(alias))
            {
                return true;
            }
        }

        return false;
    }
}
