package com.zutubi.prototype.config;

import com.zutubi.pulse.core.config.Configuration;
import com.zutubi.pulse.events.*;
import com.zutubi.util.NullaryFunction;
import com.zutubi.prototype.config.events.ConfigurationEvent;

import java.util.*;

/**
 * A simple implementation of the config provider for testing.  Does not yet
 * support all functionality (add as necessary).
 */
@SuppressWarnings({ "unchecked" })
public class MockConfigurationProvider implements ConfigurationProvider
{
    private Map<String, Configuration> instancesByPath = new HashMap<String, Configuration>();
    private Map<Class<? extends Configuration>, List<Configuration>> instancesByClass = new HashMap<Class<? extends Configuration>, List<Configuration>>();

    private MultiplexingListener syncMux;
    private MultiplexingListener asyncMux;

    public void init()
    {
        syncMux = new MultiplexingListener(ConfigurationEvent.class);
        asyncMux = new MultiplexingListener(ConfigurationEvent.class);
    }

    public <T extends Configuration> T get(String path, Class<T> clazz)
    {
        return (T) instancesByPath.get(path);
    }

    public <T extends Configuration> T get(Class<T> clazz)
    {
        return getAll(clazz).iterator().next();
    }

    public <T extends Configuration> Collection<T> getAll(String path, Class<T> clazz)
    {
        throw new RuntimeException("Method not yet implemented.");
    }

    public <T extends Configuration> Collection<T> getAll(Class<T> clazz)
    {
        List<Configuration> found = instancesByClass.get(clazz);
        if(found == null)
        {
            return Collections.EMPTY_LIST;
        }
        else
        {
            LinkedList<T> result = new LinkedList<T>();
            for(Configuration c: found)
            {
                result.add((T) c);
            }

            return result;
        }
    }

    public <T extends Configuration> T getAncestorOfType(Configuration c, Class<T> clazz)
    {
        throw new RuntimeException("Method not yet implemented.");
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

    public void registerEventListener(ConfigurationEventListener listener, boolean synchronous, boolean includeChildPaths, Class clazz)
    {
        // slight similification - dont worry so much about the path..
        registerEventListener(listener, synchronous, clazz);
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

    public void unregisterEventListener(ConfigurationEventListener listener)
    {

    }

    public <T extends Configuration> T deepClone(T instance)
    {
        throw new RuntimeException("Method not yet implemented.");
    }

    public String insert(String path, Object instance)
    {
        instancesByPath.put(path, (Configuration) instance);

        Class<? extends Configuration> clazz = (Class<? extends Configuration>) instance.getClass();
        List<Configuration> configs = instancesByClass.get(clazz);
        if(configs == null)
        {
            configs = new LinkedList<Configuration>();
            instancesByClass.put(clazz, configs);
        }

        configs.add((Configuration) instance);
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
    
    private static class Listener implements com.zutubi.pulse.events.EventListener
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
