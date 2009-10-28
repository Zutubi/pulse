package com.zutubi.tove.config;

import com.zutubi.events.*;
import com.zutubi.tove.config.api.Configuration;
import com.zutubi.tove.config.events.ConfigurationEvent;
import com.zutubi.tove.events.ConfigurationEventSystemStartedEvent;
import com.zutubi.tove.events.ConfigurationSystemStartedEvent;
import com.zutubi.tove.type.CompositeType;
import com.zutubi.tove.type.TypeRegistry;
import com.zutubi.tove.type.record.PathUtils;
import com.zutubi.util.NullaryFunction;
import com.zutubi.util.Predicate;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadFactory;

/**
 * Default implementation of the ConfigurationProvider interface, mostly
 * delegates to the ConfigurationPersistenceManager, but also manages simple
 * ConfigurationEvent listeners.
 */
public class DefaultConfigurationProvider implements ConfigurationProvider
{
    private TypeRegistry typeRegistry;
    private ConfigurationPersistenceManager configurationPersistenceManager;
    private ConfigurationTemplateManager configurationTemplateManager;
    private ConfigurationStateManager configurationStateManager;
    private EventManager eventManager;
    private ThreadFactory threadFactory;
    private DemultiplexingListener syncMux;
    private DemultiplexingListener asyncMux;

    public void init()
    {
        syncMux = new DemultiplexingListener(ConfigurationEvent.class);
        eventManager.register(syncMux);

        asyncMux = new DemultiplexingListener(ConfigurationEvent.class);
        AsynchronousDelegatingListener asych = new AsynchronousDelegatingListener(asyncMux, threadFactory);
        eventManager.register(asych);

        // The following initialisation steps, along with some of the
        // initialisation now living in the setup manager, should be
        // refactored into a separate high-level configuration manager.
        configurationTemplateManager.initSecondPhase();

        // The last thing we want to do during init is to ensure that we have
        // state objects that match all of our configuration objects.  These
        // may be out of sync due to:
        // a) a restore from a backup that does not contain database export
        // b) a restart after an external database issue resulted in a rollback
        // c) any number of other options.
        ensureExternalStateIsInSyncWithConfiguration();

        // Two events as the first is used to tie in listeners so that
        // handlers of the second are free to make changes (which will then
        // be visible to said listeners).
        eventManager.publish(new ConfigurationEventSystemStartedEvent(this));
        eventManager.publish(new ConfigurationSystemStartedEvent(this));
    }

    private void ensureExternalStateIsInSyncWithConfiguration()
    {
        // do we want to wrap this process in a transaction?
        List<Class<? extends Configuration>> statefulTypes = configurationStateManager.getStatefulConfigurationTypes();
        for (Class<? extends Configuration> clazz : statefulTypes)
        {
            // configuration provider is not available at this state of proceedings, so go directly to the ctm.
            Collection<? extends Configuration> configs = configurationTemplateManager.getAllInstances(clazz, false);
            for (Configuration config : configs)
            {
                Object externalState = configurationStateManager.getExternalState(config);
                if (externalState == null && config.isConcrete())
                {
                    configurationStateManager.createAndAssignState(config);
                }
            }
        }
    }

    public <T extends Configuration> T get(long handle, Class<T> clazz)
    {
        return configurationTemplateManager.getInstance(handle, clazz);
    }

    public <T extends Configuration> T get(String path, Class<T> clazz)
    {
        return configurationTemplateManager.getInstance(path, clazz);
    }

    public <T extends Configuration> T get(Class<T> clazz)
    {
        Collection<T> instances = getAll(clazz);
        if(instances.size() > 0)
        {
            return instances.iterator().next();
        }

        return null;
    }

    public <T extends Configuration> Collection<T> getAll(String path, Class<T> clazz)
    {
        return configurationTemplateManager.getAllInstances(path, clazz, false);
    }

    public <T extends Configuration> Collection<T> getAll(Class<T> clazz)
    {
        return configurationTemplateManager.getAllInstances(clazz, false);
    }

    public <T extends Configuration> T getAncestorOfType(Configuration c, Class<T> clazz)
    {
        return configurationTemplateManager.getAncestorOfType(c, clazz);
    }

    public <T extends Configuration> T getAncestorOfType(String path, Class<T> clazz)
    {
        if (path == null)
        {
            return null;
        }
        
        Configuration instance = get(path, Configuration.class);
        while (instance == null)
        {
            path = PathUtils.getParentPath(path);
            if (path == null)
            {
                return null;
            }
            instance = get(path, Configuration.class);
        }
        return getAncestorOfType(instance, clazz);
    }

    public <T extends Configuration> Set<T> getAllDescendents(String path, Class<T> clazz, boolean strict, boolean concreteOnly)
    {
        List<String> descendentPaths = configurationTemplateManager.getDescendentPaths(path, strict, concreteOnly, false);
        Set<T> result = new HashSet<T>(descendentPaths.size());
        for (String descendentPath: descendentPaths)
        {
            result.add(get(descendentPath, clazz));
        }

        return result;
    }

    public boolean isDeeplyValid(String path)
    {
        return configurationTemplateManager.isDeeplyValid(path);
    }

    public String insert(String parentPath, Configuration instance)
    {
        return configurationTemplateManager.insert(parentPath, instance);
    }

    public String save(Configuration instance)
    {
        return configurationTemplateManager.save(instance);
    }

    public void delete(String path)
    {
        configurationTemplateManager.delete(path);
    }

    public <T> T executeInsideTransaction(final NullaryFunction<T> f)
    {
        return configurationTemplateManager.executeInsideTransaction(new ConfigurationTemplateManager.Action<T>()
        {
            public T execute() throws Exception
            {
                return f.process();
            }
        });
    }

    public void registerEventListener(ConfigurationEventListener listener, boolean synchronous, boolean includeChildPaths, Class clazz)
    {
        CompositeType type = typeRegistry.getType(clazz);
        if(type == null)
        {
            throw new IllegalArgumentException("Class '" + clazz.getName() + "' is not registered as a configuration type");
        }

        List<String> paths = configurationPersistenceManager.getConfigurationPaths(type);
        registerEventListener(listener, synchronous, includeChildPaths, paths.toArray(new String[paths.size()]));
    }

    public void registerEventListener(ConfigurationEventListener listener, boolean synchronous, boolean includeChildPaths, String... paths)
    {
        FilteringListener filter = new FilteringListener(new PathPredicate(includeChildPaths, paths), new Listener(listener));
        if (synchronous)
        {
            syncMux.addDelegate(filter);
        }
        else
        {
            asyncMux.addDelegate(filter);
        }
    }

    public void registerEventListener(ConfigurationEventListener listener, boolean synchronous, Class clazz)
    {
        ClassPredicate classPredicate = new ClassPredicate(clazz);
        FilteringListener filter = new FilteringListener(classPredicate, new Listener(listener));
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

    public <T extends Configuration> T deepClone(T instance)
    {
        return configurationTemplateManager.deepClone(instance);
    }

    private void unregister(final ConfigurationEventListener listener, DemultiplexingListener mux)
    {
        mux.removeDelegate(new Predicate<EventListener>()
        {
            public boolean satisfied(EventListener eventListener)
            {
                return listener == ((Listener)eventListener).getDelegate();
            }
        });
    }

    public void setTypeRegistry(TypeRegistry typeRegistry)
    {
        this.typeRegistry = typeRegistry;
    }

    public void setConfigurationPersistenceManager(ConfigurationPersistenceManager configurationPersistenceManager)
    {
        this.configurationPersistenceManager = configurationPersistenceManager;
    }

    public void setConfigurationTemplateManager(ConfigurationTemplateManager configurationTemplateManager)
    {
        this.configurationTemplateManager = configurationTemplateManager;
    }

    public void setEventManager(EventManager eventManager)
    {
        this.eventManager = eventManager;
    }

    public void setThreadFactory(ThreadFactory threadFactory)
    {
        this.threadFactory = threadFactory;
    }

    public void setConfigurationStateManager(ConfigurationStateManager configurationStateManager)
    {
        this.configurationStateManager = configurationStateManager;
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
