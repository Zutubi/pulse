package com.zutubi.pulse.master.agent;

import com.zutubi.events.Event;
import com.zutubi.events.EventListener;
import com.zutubi.events.EventManager;
import com.zutubi.pulse.master.events.AgentStatusChangeEvent;
import com.zutubi.pulse.master.model.AgentSynchronisationMessage;
import com.zutubi.pulse.servercore.agent.SynchronisationMessage;
import com.zutubi.pulse.servercore.agent.SynchronisationMessageResult;
import com.zutubi.pulse.servercore.util.background.BackgroundServiceSupport;
import static com.zutubi.util.CollectionUtils.filter;
import static com.zutubi.util.CollectionUtils.map;
import com.zutubi.util.InvertedPredicate;
import com.zutubi.util.Mapping;
import com.zutubi.util.Predicate;
import com.zutubi.util.logging.Logger;

import java.util.List;

/**
 * Responsible for handling the {@link AgentStatus#SYNCHRONISING} state of
 * agents.  This state occurs just after an agent comes online or finishes a
 * build, but before it becomes available for building.  In this time window
 * various tasks may be undertaken, e.g. cleaning up unwanted directories.
 * <p/>
 * This service ensures that the tasks are run on entering this state, and that
 * the appropriate event is raised when synchronisation is complete.
 */
public class AgentSynchronisationService extends BackgroundServiceSupport implements EventListener
{
    private static final Logger LOG = Logger.getLogger(AgentSynchronisationService.class);

    public static final int COMPLETED_MESSAGE_LIMIT = 10;

    private AgentManager agentManager;

    public AgentSynchronisationService()
    {
        super("Agent Synchronisation");
    }

    private void syncAgent(final Agent agent)
    {
        getExecutorService().submit(new Runnable()
        {
            public void run()
            {
                boolean successful = true;
                try
                {
                    List<AgentSynchronisationMessage> messages = agentManager.getSynchronisationMessages(agent);
                    List<AgentSynchronisationMessage> pendingMessages = filter(messages, new PendingMessagesPredicate());
                    if (pendingMessages.size() > 0)
                    {
                        setMessagesToProcessing(pendingMessages);
                        successful = sendPendingMessages(pendingMessages);
                    }

                    cleanupOldCompletedMessages(messages);
                }
                catch (Exception e)
                {
                    LOG.warning("Unexpected exception synchronising agent '" + agent.getName() + "': " + e.getMessage(), e);
                }
                finally
                {
                    if (!agentManager.completeSynchronisation(agent, successful))
                    {
                        // More messages have come in, go around again.
                        syncAgent(agent);
                    }
                }
            }

            private void setMessagesToProcessing(List<AgentSynchronisationMessage> pendingMessages)
            {
                for (AgentSynchronisationMessage message: pendingMessages)
                {
                    message.startProcessing();
                }

                agentManager.saveSynchronisationMessages(pendingMessages);
            }

            private boolean sendPendingMessages(List<AgentSynchronisationMessage> pendingMessages)
            {
                List<SynchronisationMessage> toSend = map(pendingMessages, new ExtractMessageMapping());

                AgentService service = agent.getService();
                List<SynchronisationMessageResult> results;
                try
                {
                    results = service.synchronise(toSend);
                }
                catch (Exception e)
                {
                    // Could not complete the service call.  Mark messages for retry later.
                    LOG.warning("Unabled to synchronise agent '" + agent.getName() + "': " + e.getMessage(), e);
                    for (AgentSynchronisationMessage pending : pendingMessages)
                    {
                        pending.applySendingException(e);
                    }
                    agentManager.saveSynchronisationMessages(pendingMessages);

                    return false;
                }

                applyResults(results, pendingMessages);
                return true;
            }

            private void applyResults(List<SynchronisationMessageResult> results, List<AgentSynchronisationMessage> pendingMessages)
            {
                for (int i = 0; i < results.size(); i++)
                {
                    AgentSynchronisationMessage pending = pendingMessages.get(i);
                    SynchronisationMessageResult result = results.get(i);
                    pending.applyResult(result);
                }
                agentManager.saveSynchronisationMessages(pendingMessages);
            }

            private void cleanupOldCompletedMessages(List<AgentSynchronisationMessage> messages)
            {
                List<AgentSynchronisationMessage> completed = filter(messages, new InvertedPredicate<AgentSynchronisationMessage>(new PendingMessagesPredicate()));
                for (int i = 0; i < completed.size() - COMPLETED_MESSAGE_LIMIT; i++)
                {
                    agentManager.dequeueSynchronisationMessage(completed.get(i));
                }
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
        return new Class[]{AgentStatusChangeEvent.class};
    }

    public void setAgentManager(AgentManager agentManager)
    {
        this.agentManager = agentManager;
    }

    public void setEventManager(EventManager eventManager)
    {
        eventManager.register(this);
    }

    public static class PendingMessagesPredicate implements Predicate<AgentSynchronisationMessage>
    {
        public boolean satisfied(AgentSynchronisationMessage message)
        {
            switch (message.getStatus())
            {
                case FAILED_PERMANENTLY:
                case SUCCEEDED:
                    return false;
                default:
                    return true;
            }
        }
    }

    private static class ExtractMessageMapping implements Mapping<AgentSynchronisationMessage, SynchronisationMessage>
    {
        public SynchronisationMessage map(AgentSynchronisationMessage message)
        {
            return message.getMessage();
        }
    }
}
