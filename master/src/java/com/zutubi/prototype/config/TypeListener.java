package com.zutubi.prototype.config;

import com.zutubi.prototype.config.events.ConfigurationEvent;
import com.zutubi.prototype.config.events.PostDeleteEvent;
import com.zutubi.prototype.config.events.PostInsertEvent;
import com.zutubi.prototype.config.events.PostSaveEvent;
import com.zutubi.pulse.core.config.Configuration;

/**
 * A type listener is a convenient way to listen for all events that affect
 * instances of a given type.  It hides details such as where the types can
 * be located and also raises changed events when a child is inserted,
 * deleted or changed.
 */
public abstract class TypeListener<T extends Configuration> implements ConfigurationEventListener
{
    private Class<T> configurationClass;

    public TypeListener(Class<T> configurationClass)
    {
        this.configurationClass = configurationClass;
    }

    public void register(final ConfigurationProvider configurationProvider)
    {
        configurationProvider.registerEventListener(this, true, true, configurationClass);
    }

    public void handleConfigurationEvent(ConfigurationEvent event)
    {
        Object instance = event.getInstance();
        // We take advantage of the fact that a type is only allowed to
        // appear once in a path.  Thus if we have the type we expected, we
        // know it has no ancestor of the same type to worry about.
        if(configurationClass.isInstance(instance))
        {
            // Change occured directly to an instance of our type
            if (event instanceof PostInsertEvent)
            {
                postInsert((T) event.getInstance());
            }
            else if(event instanceof PostSaveEvent)
            {
                postSave((T) event.getInstance());
            }
            else if (event instanceof PostDeleteEvent)
            {
                postDelete((T) event.getInstance());
            }
        }
        else
        {
            // We ignore cascaded inserts/deleted as the event for the root
            // of the cascade is enough to trigger the desired event.
            if(!cascaded(event))
            {
                // Change occured to a child path.  No matter what happened, it
                // is seen as a change to our own path.
                T ancestor = event.getSource().getAncestorOfType((Configuration) instance, configurationClass);
                if (ancestor != null)
                {
                    postSave(ancestor);
                }
            }
        }

    }

    private boolean cascaded(ConfigurationEvent event)
    {
        if(event instanceof PostInsertEvent)
        {
            return ((PostInsertEvent)event).isCascaded();
        }
        else if(event instanceof PostDeleteEvent)
        {
            return ((PostDeleteEvent)event).isCascaded();
        }
        else
        {
            return false;
        }
    }

    public abstract void postInsert(T instance);
    public abstract void postDelete(T instance);
    public abstract void postSave(T instance);
}
