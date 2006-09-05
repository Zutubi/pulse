package com.zutubi.pulse.plugins.descriptors;

import com.zutubi.plugins.internal.ComponentDescriptorSupport;
import com.zutubi.pulse.events.EventManager;
import com.zutubi.pulse.events.EventListener;
import com.zutubi.pulse.core.ObjectFactory;
import nu.xom.Element;


/**
 * <class-comment/>
 */
public class EventListenerComponentDescriptor extends ComponentDescriptorSupport
{
    private String listenerClassName;

    private EventManager eventManager;

    private EventListener instance;
    
    private ObjectFactory objectFactory;

    public void init(Element config)
    {
        super.init(config);

        setListenerClassName(config.getAttributeValue("class"));
    }

    public void setListenerClassName(String className)
    {
        this.listenerClassName = className;
    }

    public String getListenerClassName()
    {
        return listenerClassName;
    }

    protected synchronized EventListener getInstance()
    {
        if (instance == null)
        {
            try
            {
                Class cls = plugin.loadClass(listenerClassName, EventListenerComponentDescriptor.class);
                instance = objectFactory.buildBean(cls);
            }
            catch (Exception e)
            {
                e.printStackTrace();
                throw new RuntimeException();
            }
        }
        return instance;
    }

    /**
     * Enable the listener component by registering it with the systems event manager.
     */
    public void enable()
    {
        super.enable();

        eventManager.register(getInstance());
    }

    /**
     * Disable the listener component by unregistering it from the systems event manager.
     */
    public void disable()
    {
        super.disable();

        eventManager.unregister(getInstance());
    }

    /**
     * Required resource.
     *
     * @param eventManager
     */
    public void setEventManager(EventManager eventManager)
    {
        this.eventManager = eventManager;
    }

    /**
     * Required resource.
     *
     * @param objectFactory
     */
    public void setObjectFactory(ObjectFactory objectFactory)
    {
        this.objectFactory = objectFactory;
    }
}
