package com.zutubi.tove.config;

import com.zutubi.events.DemultiplexingListener;
import com.zutubi.events.Event;
import com.zutubi.events.FilteringListener;
import com.zutubi.tove.config.api.Configuration;
import com.zutubi.tove.config.events.ConfigurationEvent;
import com.zutubi.util.NullaryFunction;

import java.util.*;

/**
 * A simple implementation of the config provider for testing.  Does not yet
 * support all functionality (add as necessary).
 */
public class FakeConfigurationProvider implements ConfigurationProvider
{
    private Map<String, Configuration> instancesByPath = new HashMap<String, Configuration>();
    private Map<Class<? extends Configuration>, List<Configuration>> instancesByClass = new HashMap<Class<? extends Configuration>, List<Configuration>>();

    private DemultiplexingListener syncMux;
    private DemultiplexingListener asyncMux;

    public FakeConfigurationProvider()
    {
        syncMux = new DemultiplexingListener(ConfigurationEvent.class);
        asyncMux = new DemultiplexingListener(ConfigurationEvent.class);
    }

    public <T extends Configuration> T get(String path, Class<T> clazz)
    {
        return clazz.cast(instancesByPath.get(path));
    }

    public <T extends Configuration> T get(Class<T> clazz)
    {
        return getAll(clazz).iterator().next();
    }

    public <T extends Configuration> Collection<T> getAll(String path, Class<T> clazz)
    {
        throw new RuntimeException("Method not yet implemented.");
    }

    public <T extends Configuration> T get(long handle, Class<T> clazz)
    {
        throw new RuntimeException("Method not yet implemented.");
    }

    public <T extends Configuration> Collection<T> getAll(Class<T> clazz)
    {
        List<Configuration> found = instancesByClass.get(clazz);
        if(found == null)
        {
            return Collections.emptyList();
        }
        else
        {
            LinkedList<T> result = new LinkedList<T>();
            for(Configuration c: found)
            {
                result.add(clazz.cast(c));
            }

            return result;
        }
    }

    public <T extends Configuration> T getAncestorOfType(Configuration c, Class<T> clazz)
    {
        throw new RuntimeException("Method not yet implemented.");
    }

    public <T extends Configuration> T getAncestorOfType(String path, Class<T> clazz)
    {
        throw new RuntimeException("Method not yet implemented.");
    }

    public <T extends Configuration> Set<T> getAllDescendants(String path, Class<T> clazz, boolean strict, boolean concrete)
    {
        throw new RuntimeException("Not implemented");
    }

    public boolean isDeeplyValid(String path)
    {
        throw new RuntimeException("Method not yet implemented");
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

    public void registerEventListener(ConfigurationEventListener listener, boolean synchronous, boolean includeChildPaths, Class<? extends Configuration> clazz)
    {
        // slight similification - dont worry so much about the path..
        registerEventListener(listener, synchronous, clazz);
    }

    public void registerEventListener(ConfigurationEventListener listener, boolean synchronous, Class<? extends Configuration> clazz)
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

    public void unregisterEventListener(ConfigurationEventListener listener)
    {

    }

    public <T extends Configuration> T deepClone(T instance)
    {
        throw new RuntimeException("Method not yet implemented.");
    }

    public String insert(String path, Configuration instance)
    {
        instancesByPath.put(path, instance);

        Class<? extends Configuration> clazz = instance.getClass();
        List<Configuration> configs = instancesByClass.get(clazz);
        if(configs == null)
        {
            configs = new LinkedList<Configuration>();
            instancesByClass.put(clazz, configs);
        }

        configs.add(instance);
        return path;
    }

    public String save(Configuration instance)
    {
        throw new RuntimeException("Method not yet implemented.");
    }

    public void delete(String path)
    {
        throw new RuntimeException("Method not yet implemented.");
    }

    public <T> T executeInsideTransaction(NullaryFunction<T> f)
    {
        throw new RuntimeException("Method not yet implemented");
    }

    public void sendEvent(ConfigurationEvent evt)
    {
        syncMux.handleEvent(evt);
        asyncMux.handleEvent(evt);
    }
    
    private static class Listener implements com.zutubi.events.EventListener
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
