package com.zutubi.prototype.config;

import com.zutubi.pulse.core.config.Configuration;
import com.zutubi.prototype.config.events.ConfigurationEvent;
import com.zutubi.prototype.config.events.PostInsertEvent;
import com.zutubi.prototype.config.events.PostSaveEvent;
import com.zutubi.prototype.config.events.PostDeleteEvent;

/**
 *
 *
 */
public abstract class TypeListener<T extends Configuration>
{
    private Class<T> configurationClass;

    public TypeListener(Class<T> configurationClass)
    {
        this.configurationClass = configurationClass;
    }

    public void register(final ConfigurationProvider configurationProvider)
    {
        // some of these are async events.  Confusing. Review.
        configurationProvider.registerEventListener(new ConfigurationEventListener()
        {
            public void handleConfigurationEvent(ConfigurationEvent event)
            {
                if (event instanceof PostInsertEvent)
                {
                    postInsert((T) ((PostInsertEvent)event).getNewInstance());
                }
                if (event instanceof PostSaveEvent)
                {
                    postSave((T) ((PostSaveEvent)event).getNewInstance());
                }
                else if (event instanceof PostDeleteEvent)
                {
                    postDelete((T) ((PostDeleteEvent)event).getOldInstance());
                }
            }
        }, false, configurationClass);
    }

    /**
     *
     * @param instance
     */
    public abstract void postInsert(T instance);

    /**
     *
     * @param instance
     */
    public abstract void postDelete(T instance);

    /**
     *
     * @param instance
     */
    public abstract void postSave(T instance);
    
}
