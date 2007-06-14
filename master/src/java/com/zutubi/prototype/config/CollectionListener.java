package com.zutubi.prototype.config;

import com.zutubi.prototype.config.events.*;
import com.zutubi.prototype.type.record.MutableRecord;
import com.zutubi.prototype.type.record.PathUtils;
import com.zutubi.pulse.core.config.Configuration;

/**
 */
@SuppressWarnings({"unchecked"})
public abstract class CollectionListener<T extends Configuration>
{
    private String path;
    private Class<T> configurationClass;
    private boolean synchronous;

    protected CollectionListener(String path, Class<T> configurationClass, boolean synchronous)
    {
        this.path = path;
        this.configurationClass = configurationClass;
        this.synchronous = synchronous;
    }

    public void register(final ConfigurationProvider configurationProvider)
    {
        configurationProvider.registerEventListener(new ConfigurationEventListener()
        {
            public void handleConfigurationEvent(ConfigurationEvent event)
            {
                if(event instanceof PreInsertEvent)
                {
                    preInsert(((PreInsertEvent)event).getRecord());
                }
                else if(event instanceof PostInsertEvent)
                {
                    Object instance = ((PostInsertEvent)event).getNewInstance();
                    if(configurationClass.isInstance(instance))
                    {
                        instanceInserted((T)instance);
                    }
                }
                else if(event instanceof PreDeleteEvent)
                {
                    Object instance = ((PreDeleteEvent)event).getInstance();
                    if(configurationClass.isInstance(instance))
                    {
                        instanceDeleted((T)instance);
                    }
                }
            }
        }, synchronous, false, PathUtils.getPath(path, PathUtils.WILDCARD_ANY_ELEMENT));

        // And finally a listener for changes to the object, which is also
        // tripped by changes to the child path.
        configurationProvider.registerEventListener(new ConfigurationEventListener()
        {
            public void handleConfigurationEvent(ConfigurationEvent event)
            {
                if(event instanceof PostSaveEvent)
                {
                    Configuration instance = (Configuration) ((PostSaveEvent)event).getNewInstance();
                    T t = configurationProvider.getAncestorOfType(instance, configurationClass);
                    if(t != null)
                    {
                        instanceChanged(t);
                    }
                }
            }
        }, synchronous, true, path);

/*
        FIXME: need to fix this so that instanceChange callbacks are generated when nested paths of
               the configured path are changed / added / deleted.
        configurationProvider.registerEventListener(new ConfigurationEventListener()
        {
            public void handleConfigurationEvent(ConfigurationEvent event)
            {
                if(event instanceof PostInsertEvent)
                {
                    Configuration instance = (Configuration) ((PostInsertEvent)event).getNewInstance();
                    T t = configurationProvider.getAncestorOfType(instance, configurationClass);
                    if(t != null)
                    {
                        instanceChanged(t);
                    }
                }
            }
        }, synchronous, true, PathUtils.getPath(path, PathUtils.WILDCARD_ANY_ELEMENT));
*/
    }

    protected abstract void preInsert(MutableRecord record);
    protected abstract void instanceInserted(T instance);
    protected abstract void instanceDeleted(T instance);
    protected abstract void instanceChanged(T instance);
}
