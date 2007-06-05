package com.zutubi.prototype.config;

import com.zutubi.pulse.events.Event;
import com.zutubi.util.Predicate;
import com.zutubi.prototype.config.events.ConfigurationEvent;
import com.zutubi.prototype.config.events.PreInsertEvent;
import com.zutubi.prototype.config.events.PreSaveEvent;
import com.zutubi.prototype.config.events.PreDeleteEvent;
import com.zutubi.prototype.config.events.PostInsertEvent;
import com.zutubi.prototype.config.events.PostDeleteEvent;
import com.zutubi.prototype.config.events.PostSaveEvent;

/**
 *
 *
 */
public class ClassPredicate implements Predicate<Event>
{
    private Class clazz;

    public ClassPredicate(Class clazz)
    {
        this.clazz = clazz;
    }

    public boolean satisfied(Event event)
    {
        if (!(event instanceof ConfigurationEvent))
        {
            return false;
        }

        if (event instanceof PreInsertEvent)
        {
//            ((PreInsertEvent)event);
        }
        else if (event instanceof PreSaveEvent)
        {
            return clazz.isAssignableFrom(((PreSaveEvent)event).getOldInstance().getClass());
        }
        else if (event instanceof PreDeleteEvent)
        {
            return clazz.isAssignableFrom(((PreDeleteEvent)event).getInstance().getClass());
        }
        else if (event instanceof PostInsertEvent)
        {
            return clazz.isAssignableFrom(((PostInsertEvent)event).getNewInstance().getClass());
        }
        else if (event instanceof PostDeleteEvent)
        {
            return clazz.isAssignableFrom(((PostDeleteEvent)event).getOldInstance().getClass());
        }
        else if (event instanceof PostSaveEvent)
        {
            return clazz.isAssignableFrom(((PostSaveEvent)event).getOldInstance().getClass());
        }

        return false;
    }


}
