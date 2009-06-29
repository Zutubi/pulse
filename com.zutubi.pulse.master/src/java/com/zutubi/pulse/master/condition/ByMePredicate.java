package com.zutubi.pulse.master.condition;

import com.zutubi.pulse.core.model.PersistentChangelist;
import com.zutubi.pulse.master.tove.config.user.UserConfiguration;
import com.zutubi.util.Predicate;

/**
 * A predicate to test if a changelist includes is by a given user.
 */
public class ByMePredicate implements Predicate<PersistentChangelist>
{
    private UserConfiguration user;

    public ByMePredicate(UserConfiguration user)
    {
        this.user = user;
    }

    public boolean satisfied(PersistentChangelist changelist)
    {
        if (changelist.getChanges() == null || changelist.getChanges().isEmpty())
        {
            return false;
        }

        String author = changelist.getAuthor();
        if(author.equals(user.getLogin()))
        {
            return true;
        }

        for(String alias: user.getPreferences().getAliases())
        {
            if(author.equals(alias))
            {
                return true;
            }
        }

        return false;
    }
}
