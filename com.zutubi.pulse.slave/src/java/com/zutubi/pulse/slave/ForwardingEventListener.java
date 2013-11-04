package com.zutubi.pulse.slave;

import com.zutubi.events.Event;
import com.zutubi.events.EventListener;
import com.zutubi.events.EventManager;
import com.zutubi.pulse.core.events.GenericOutputEvent;
import com.zutubi.pulse.core.events.RecipeEvent;
import com.zutubi.pulse.servercore.events.SynchronisationMessageProcessedEvent;
import com.zutubi.pulse.servercore.services.MasterService;
import com.zutubi.pulse.servercore.services.ServiceTokenManager;
import com.zutubi.util.logging.Logger;

/**
 * An event listener that forwards relevant events to the master server.
 */
public class ForwardingEventListener implements EventListener
{
    private static final Logger LOG = Logger.getLogger(ForwardingEventListener.class);

    private String masterUrl;
    private MasterService masterService;
    private ServiceTokenManager serviceTokenManager;

    public synchronized void setMaster(String masterUrl, MasterService masterService)
    {
        this.masterUrl = masterUrl;
        this.masterService = masterService;
    }
    
    public void handleEvent(Event event)
    {
        String masterUrl;
        MasterService masterService;
        synchronized (this)
        {
            masterUrl = this.masterUrl;
            masterService = this.masterService;
        }
        
        if (masterService != null)
        {
            try
            {
                masterService.handleEvent(serviceTokenManager.getToken(), event);
            }
            catch (Exception e)
            {
                // TODO abort the recipe execution?
                // TODO support retrying events
                LOG.severe("Could not forward event to master.  Check master location (" + masterUrl + ") is accessible from this agent", e);
            }
        }
        else
        {
            LOG.severe("Could not forward event to master: no master registered");
        }
    }

    public Class[] getHandledEvents()
    {
        return new Class[]{ GenericOutputEvent.class, RecipeEvent.class, SynchronisationMessageProcessedEvent.class};
    }

    public void setEventManager(EventManager eventManager)
    {
        eventManager.register(this);
    }
    
    public void setServiceTokenManager(ServiceTokenManager serviceTokenManager)
    {
        this.serviceTokenManager = serviceTokenManager;
    }
}
