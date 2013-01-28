package com.zutubi.tove.config;

import com.google.common.base.Predicate;
import com.zutubi.events.Event;
import com.zutubi.tove.config.events.ConfigurationEvent;

/**
 * A predicate to test for configuration events against instances of a give class (including
 * subclasses).
 */
public class ClassPredicate implements Predicate<Event>
{
    private Class<?> clazz;

    public ClassPredicate(Class<?> clazz)
    {
        if (clazz == null)
        {
            throw new IllegalArgumentException();
        }
        this.clazz = clazz;
    }

    public boolean apply(Event event)
    {
        if (!(event instanceof ConfigurationEvent))
        {
            return false;
        }

        return clazz.isAssignableFrom(((ConfigurationEvent)event).getInstance().getClass());
    }
}
