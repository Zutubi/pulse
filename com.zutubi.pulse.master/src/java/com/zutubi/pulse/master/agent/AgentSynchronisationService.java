package com.zutubi.pulse.master.agent;

import com.zutubi.events.Event;
import com.zutubi.events.EventListener;
import com.zutubi.events.EventManager;
import com.zutubi.pulse.master.events.AgentStatusChangeEvent;
import com.zutubi.pulse.master.events.AgentSynchronisationCompleteEvent;
import com.zutubi.pulse.servercore.util.background.BackgroundServiceSupport;

/**
 * Responsible for handling the {@link AgentStatus#SYNCHRONISING} state of
 * agents.  This state occurs just after an agent comes online, but before it
 * becomes available for building.  In this time window various tasks may be
 * undertaken, e.g. the sending of pending messages.
 * <p/>
 * This service ensures that the tasks are run on entering this state, and that
 * the appropriate event is raised when synchronisation is complete.
 */
public class AgentSynchronisationService extends BackgroundServiceSupport implements EventListener
{
    private EventManager eventManager;

    public AgentSynchronisationService()
    {
        super("Agent Synchronisation");
    }

    private void syncAgent(final Agent agent)
    {
        getExecutorService().execute(new Runnable()
        {
            public void run()
            {
                eventManager.publish(new AgentSynchronisationCompleteEvent(this, agent, true));
            }
        });
    }

    public void handleEvent(Event event)
    {
        AgentStatusChangeEvent asce = (AgentStatusChangeEvent) event;
        if (asce.getNewStatus() == AgentStatus.SYNCHRONISING)
        {
            syncAgent(asce.getAgent());
        }
    }

    public Class[] getHandledEvents()
    {
        return new Class[]{ AgentStatusChangeEvent.class };
    }

    public void setEventManager(EventManager eventManager)
    {
        eventManager.register(this);
        this.eventManager = eventManager;
    }
}
