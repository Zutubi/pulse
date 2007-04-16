package com.zutubi.prototype.config;

import com.zutubi.prototype.config.events.ConfigurationEvent;
import com.zutubi.prototype.type.CompositeType;
import com.zutubi.prototype.type.TypeRegistry;
import com.zutubi.pulse.events.*;
import com.zutubi.pulse.util.Predicate;

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
    private MultiplexingListener muxListener;
    private TypeRegistry typeRegistry;

    public void init()
    {
        muxListener = new MultiplexingListener(ConfigurationEvent.class);
        AsynchronousDelegatingListener asych = new AsynchronousDelegatingListener(muxListener);
        eventManager.register(asych);

        configurationPersistenceManager.registerListener("", new EventPublishingConfigurationListener(eventManager));
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

    public void registerListener(Class clazz, ConfigurationListener listener)
    {
        configurationPersistenceManager.registerListener(clazz, listener);
    }

    public void registerListener(String path, ConfigurationListener listener)
    {
        configurationPersistenceManager.registerListener(path, listener);
    }

    public void unregisterListener(ConfigurationListener listener)
    {
        configurationPersistenceManager.unregisterListener(listener);
    }

    public void registerEventListener(ConfigurationEventListener listener, Class clazz)
    {
        CompositeType type = typeRegistry.getType(clazz);
        if(type == null)
        {
            throw new IllegalArgumentException("Class '" + clazz.getName() + "' is not registered as a configuration type");
        }

        List<String> paths = configurationPersistenceManager.getConfigurationPaths(type);
        FilteringListener filter = new FilteringListener(new PathPredicate(paths.toArray(new String[paths.size()])), new Listener(listener));
        muxListener.addDelegate(filter);
    }

    public void registerEventListener(ConfigurationEventListener listener, String... paths)
    {
        FilteringListener filter = new FilteringListener(new PathPredicate(paths), new Listener(listener));
        muxListener.addDelegate(filter);
    }

    public void unregisterEventListener(final ConfigurationEventListener listener)
    {
        muxListener.removeDelegate(new Predicate<EventListener>()
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
            delegate.handleEvent((ConfigurationEvent)evt);
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
