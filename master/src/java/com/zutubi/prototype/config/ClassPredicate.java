package com.zutubi.prototype.config;

import com.zutubi.prototype.config.events.ConfigurationEvent;
import com.zutubi.pulse.events.Event;
import com.zutubi.util.Predicate;

/**
 *
 *
 */
public class ClassPredicate implements Predicate<Event>
{
    private Class clazz;

    public ClassPredicate(Class clazz)
    {
        if (clazz == null)
        {
            throw new IllegalArgumentException();
        }
        this.clazz = clazz;
    }

    public boolean satisfied(Event event)
    {
        if (!(event instanceof ConfigurationEvent))
        {
            return false;
        }

        return clazz.isAssignableFrom(((ConfigurationEvent)event).getInstance().getClass());
    }
}
