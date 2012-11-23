package com.zutubi.events;

import java.util.*;

/**
 * A default implementation of {@link EventManager}.
 */
public class DefaultEventManager implements EventManager
{
    private final Map<Class, List<EventListener>> typeToListener = new HashMap<Class, List<EventListener>>();
    private final ThreadLocal<DeferredQueue> publishThreadLocal = new ThreadLocal<DeferredQueue>();

    private EventDispatcher dispatcher;

    /**
     * Creates a manager that will use synchronous event dispatch.
     */
    public DefaultEventManager()
    {
        this(new SynchronousDispatcher());
    }

    /**
     * Creates a manager that will use the given dispatcher.
     *
     * @param dispatcher dispatcher used to pass events to listeners
     */
    public DefaultEventManager(EventDispatcher dispatcher)
    {
        this.dispatcher = dispatcher;
    }

    public synchronized void register(EventListener listener)
    {
        Class[] clazzes = listener.getHandledEvents();
        if (clazzes == null || clazzes.length == 0)
        {
            clazzes = new Class[]{Event.class};
        }
        for (Class clazz : clazzes)
        {
            updateTypeToListener(clazz, listener, true);
        }
    }

    public synchronized void unregister(EventListener listener)
    {
        Class[] clazzes = listener.getHandledEvents();
        for (Class clazz : clazzes)
        {
            updateTypeToListener(clazz, listener, false);
        }
    }

    private void updateTypeToListener(Class clazz, EventListener listener, boolean register)
    {
        if (!typeToListener.containsKey(clazz))
        {
            typeToListener.put(clazz, new LinkedList<EventListener>());
        }
        List<EventListener> listeners = typeToListener.get(clazz);
        if (register)
        {
            if (!listeners.contains(listener))
            {
                listeners.add(listener);
            }
        }
        else
        {
            listeners.remove(listener);
        }
    }

    private Set<EventListener> lookupListeners(Class clazz)
    {
        Set<EventListener> listeners = new HashSet<EventListener>();
        if (typeToListener.containsKey(clazz))
        {
            listeners.addAll(typeToListener.get(clazz));
        }

        Class[] interfaces = clazz.getInterfaces();
        for (Class i : interfaces)
        {
            listeners.addAll(lookupListeners(i));
        }

        Class superClass = clazz.getSuperclass();
        if (superClass != null)
        {
            listeners.addAll(lookupListeners(superClass));
        }

        return listeners;
    }

    public void publish(Event event)
    {
        publish(event, PublishFlag.IMMEDIATE);
    }

    public void publish(Event event, PublishFlag how)
    {
        if (event == null)
        {
            return;
        }

        switch (how)
        {
            case DEFERRED:
                deferredPublish(event);
                break;
            case IMMEDIATE:
                immediatePublish(event);
                break;
        }
    }

    private void deferredPublish(Event event)
    {
        DeferredQueue context = publishThreadLocal.get();
        if (context == null)
        {
            immediatePublish(event);
        }
        else
        {
            context.push(event);
        }
    }

    private void immediatePublish(Event event)
    {
        DeferredQueue nestedContext = publishThreadLocal.get();
        DeferredQueue context = new DeferredQueue();
        publishThreadLocal.set(context);
        try
        {
            Set<EventListener> listeners;
            synchronized (this)
            {
                listeners = lookupListeners(event.getClass());
            }

            dispatcher.dispatch(event, new LinkedList<EventListener>(listeners));
        }
        finally
        {
            publishThreadLocal.set(nestedContext);
            context.publishAll();
        }
    }

    private class DeferredQueue
    {
        private List<Event> deferred = new LinkedList<Event>();

        public void push(Event event)
        {
            deferred.add(event);
        }

        public void publishAll()
        {
            for(Event event: deferred)
            {
                immediatePublish(event);
            }
        }
    }
}
