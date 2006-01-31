package com.cinnamonbob.events;

import java.util.*;

/**
 * <class-comment/>
 */
public class DefaultEventManager implements EventManager
{
    private final Map<Class, List<EventListener>> typeToListener = new HashMap<Class, List<EventListener>>();

    private EventDispatcher dispatcher;

    public DefaultEventManager()
    {
        this(new SynchronousDispatcher());
    }

    public DefaultEventManager(EventDispatcher dispatcher)
    {
        this.dispatcher = dispatcher;
    }

    public synchronized void register(EventListener listener)
    {
        Class[] clazzes = listener.getHandledEvents();
        if (clazzes == null || clazzes.length == 0)
        {
            // if no handle events are defined, then the listener will be registered to receive all events.
            clazzes = new Class[]{Object.class};
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

    public void publish(Event evt)
    {
        if (evt == null)
        {
            return;
        }

        Set<EventListener> listeners;
        synchronized (this) {
            listeners = lookupListeners(evt.getClass());
        }

        dispatcher.dispatch(evt, new LinkedList<EventListener>(listeners));
    }
}