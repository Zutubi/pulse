package com.zutubi.pulse.master.notifications.condition;

import com.google.common.base.Predicate;
import com.zutubi.pulse.core.model.PersistentChangelist;
import com.zutubi.pulse.master.tove.config.user.UserConfiguration;

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

    public boolean apply(PersistentChangelist changelist)
    {
        String author = changelist.getAuthor();
        if (author.equals(user.getLogin()))
        {
            return true;
        }

        for (String alias: user.getPreferences().getAliases())
        {
            if (author.equals(alias))
            {
                return true;
            }
        }

        return false;
    }
}
