package com.zutubi.prototype.config;

import com.zutubi.prototype.config.events.ConfigurationEvent;
import com.zutubi.prototype.type.CompositeType;
import com.zutubi.prototype.type.TypeRegistry;
import com.zutubi.pulse.events.*;
import com.zutubi.util.Predicate;

import java.util.Collection;
import java.util.List;

/**
 * Default implementation of the ConfigurationProvider interface, mostly
 * delegates to the ConfigurationPersistenceManager, but also manages simple
 * ConfigurationEvent listeners.
 */
public class DefaultConfigurationProvider implements ConfigurationProvider
{
    private ConfigurationPersistenceManager configurationPersistenceManager;
    private EventManager eventManager;
    private MultiplexingListener syncMux;
    private MultiplexingListener asyncMux;
    private TypeRegistry typeRegistry;

    public void init()
    {
        syncMux = new MultiplexingListener(ConfigurationEvent.class);
        eventManager.register(syncMux);

        asyncMux = new MultiplexingListener(ConfigurationEvent.class);
        AsynchronousDelegatingListener asych = new AsynchronousDelegatingListener(asyncMux);
        eventManager.register(asych);
    }

    public <T> T get(String path, Class<T> clazz)
    {
        return configurationPersistenceManager.getInstance(path, clazz);
    }

    public <T> T get(Class<T> clazz)
    {
        Collection<T> instances = getAll(clazz);
        if(instances.size() > 0)
        {
            return instances.iterator().next();
        }

        return null;
    }

    public <T> Collection<T> getAll(Class<T> clazz)
    {
        return configurationPersistenceManager.getAllInstances(clazz);
    }

    public void registerEventListener(ConfigurationEventListener listener, boolean synchronous, Class clazz)
    {
        CompositeType type = typeRegistry.getType(clazz);
        if(type == null)
        {
            throw new IllegalArgumentException("Class '" + clazz.getName() + "' is not registered as a configuration type");
        }

        List<String> paths = configurationPersistenceManager.getConfigurationPaths(type);
        registerEventListener(listener, synchronous, paths.toArray(new String[paths.size()]));
    }

    public void registerEventListener(ConfigurationEventListener listener, boolean synchronous, String... paths)
    {
        FilteringListener filter = new FilteringListener(new PathPredicate(paths), new Listener(listener));
        if (synchronous)
        {
            syncMux.addDelegate(filter);
        }
        else
        {
            asyncMux.addDelegate(filter);
        }
    }

    public void unregisterEventListener(final ConfigurationEventListener listener)
    {
        unregister(listener, syncMux);
        unregister(listener, asyncMux);
    }

    private void unregister(final ConfigurationEventListener listener, MultiplexingListener mux)
    {
        mux.removeDelegate(new Predicate<EventListener>()
        {
            public boolean satisfied(EventListener eventListener)
            {
                return listener == ((Listener)eventListener).getDelegate();
            }
        });
    }

    public void setConfigurationPersistenceManager(ConfigurationPersistenceManager configurationPersistenceManager)
    {
        this.configurationPersistenceManager = configurationPersistenceManager;
    }

    public void setEventManager(EventManager eventManager)
    {
        this.eventManager = eventManager;
    }

    public void setTypeRegistry(TypeRegistry typeRegistry)
    {
        this.typeRegistry = typeRegistry;
    }

    private static class Listener implements EventListener
    {
        private ConfigurationEventListener delegate;

        public Listener(ConfigurationEventListener delegate)
        {
            this.delegate = delegate;
        }

        public void handleEvent(Event evt)
        {
            delegate.handleConfigurationEvent((ConfigurationEvent)evt);
        }

        public Class[] getHandledEvents()
        {
            return new Class[]{ConfigurationEvent.class};
        }

        public ConfigurationEventListener getDelegate()
        {
            return delegate;
        }
    }
}
