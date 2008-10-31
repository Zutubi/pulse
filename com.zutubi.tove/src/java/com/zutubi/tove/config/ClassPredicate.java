package com.zutubi.tove.config;

import com.zutubi.events.Event;
import com.zutubi.tove.config.events.ConfigurationEvent;
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
