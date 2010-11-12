package com.zutubi.pulse.master.agent;

import com.zutubi.events.Event;
import com.zutubi.events.EventListener;
import com.zutubi.events.EventManager;
import com.zutubi.pulse.master.events.AgentStatusChangeEvent;
import com.zutubi.pulse.master.model.AgentSynchronisationMessage;
import com.zutubi.pulse.master.scheduling.CallbackService;
import com.zutubi.pulse.servercore.agent.SynchronisationMessage;
import com.zutubi.pulse.servercore.agent.SynchronisationMessageResult;
import com.zutubi.pulse.servercore.events.SynchronisationMessageProcessedEvent;
import com.zutubi.pulse.servercore.util.background.BackgroundServiceSupport;
import com.zutubi.util.*;
import com.zutubi.util.logging.Logger;

import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import static com.zutubi.util.CollectionUtils.filter;
import static com.zutubi.util.CollectionUtils.map;

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

    public static final StatusInPredicate PENDING_MESSAGES_PREDICATE = new StatusInPredicate(EnumSet.of(AgentSynchronisationMessage.Status.QUEUED, AgentSynchronisationMessage.Status.SENDING_FAILED));
    public static final StatusInPredicate INCOMPLETE_MESSAGES_PREDICATE = new StatusInPredicate(EnumSet.of(AgentSynchronisationMessage.Status.QUEUED, AgentSynchronisationMessage.Status.SENDING_FAILED, AgentSynchronisationMessage.Status.PROCESSING));
    public static final StatusInPredicate COMPLETED_MESSAGES_PREDICATE = new StatusInPredicate(EnumSet.of(AgentSynchronisationMessage.Status.FAILED_PERMANENTLY, AgentSynchronisationMessage.Status.SUCCEEDED));
    
    static final long MESSAGE_TIMEOUT_SECONDS = Long.getLong("pulse.agent.sync.message.timeout", 1800);
    static final long TIMEOUT_CHECK_INTERVAL = Long.getLong("pulse.agent.sync.message.timeout.interval", 300);

    /**
     * This lock allows us to lock all of the agents via obtaining the write
     * lock.  When locking individual agents obtain the read lock.
     */
    private ReadWriteLock allAgentsLock = new ReentrantReadWriteLock();
    /**
     * Maps from agent id to a lock for that agent.
     */
    private final Map<Long, Lock> agentLocks = new HashMap<Long, Lock>();

    private AgentManager agentManager;
    private EventManager eventManager;
    private CallbackService callbackService;
    private Clock clock = new SystemClock();

    public AgentSynchronisationService()
    {
        super("Agent Synchronisation");
    }

    public void init(AgentManager agentManager)
    {
        this.agentManager = agentManager;
        
        // Check for messages that were processing when the master was shut
        // down.  They should be retried.  Note that it is important that this
        // happens before the agent status manager is fired up.
        List<AgentSynchronisationMessage> processingMessages = agentManager.getProcessingSynchronisationMessages();
        for (AgentSynchronisationMessage message: processingMessages)
        {
            message.masterRestarted();
        }
        
        agentManager.saveSynchronisationMessages(processingMessages);

        super.init();
        eventManager.register(this);

        callbackService.registerCallback("Agent Synchronisation Timeout Check", new NullaryProcedure()
        {
            public void run()
            {
                checkTimeouts();
            }
        }, TIMEOUT_CHECK_INTERVAL * Constants.SECOND);
    }

    void checkTimeouts()
    {
        // As we block sync for all agents, this method must be fast.
        allAgentsLock.writeLock().lock();
        try
        {
            final long now = clock.getCurrentTimeMillis();
            final long timeoutMillis = MESSAGE_TIMEOUT_SECONDS * Constants.SECOND;

            List<AgentSynchronisationMessage> processingMessages = agentManager.getProcessingSynchronisationMessages();
            List<AgentSynchronisationMessage> timedOutMessages = CollectionUtils.filter(processingMessages, new Predicate<AgentSynchronisationMessage>()
            {
                public boolean satisfied(AgentSynchronisationMessage agentSynchronisationMessage)
                {
                    return (now - agentSynchronisationMessage.getProcessingTimestamp()) > timeoutMillis;
                }
            });

            Set<Long> affectedAgentIds = new HashSet<Long>();
            for (AgentSynchronisationMessage message: timedOutMessages)
            {
                message.timedOut(now - message.getProcessingTimestamp());
                affectedAgentIds.add(message.getAgentState().getId());
            }

            agentManager.saveSynchronisationMessages(timedOutMessages);
            
            for (Long agentId: affectedAgentIds)
            {
                tryToComplete(agentId, true);
            }
        }
        finally
        {
            allAgentsLock.writeLock().unlock();
        }
    }

    private void syncAgent(final long agentId)
    {
        getExecutorService().submit(new Runnable()
        {
            public void run()
            {
                Agent agent = agentManager.getAgentById(agentId);
                if (agent == null)
                {
                    return;
                }
                
                lockAgent(agentId);
                try
                {
                    boolean successful = true;
                    try
                    {
                        List<AgentSynchronisationMessage> messages = agentManager.getSynchronisationMessages(agentId);
                        List<AgentSynchronisationMessage> pendingMessages = filter(messages, PENDING_MESSAGES_PREDICATE);
                        if (pendingMessages.size() > 0)
                        {
                            setMessagesToProcessing(pendingMessages);
                            successful = sendPendingMessages(agent, pendingMessages);
                        }
    
                        cleanupOldCompletedMessages(messages);
                    }
                    catch (Exception e)
                    {
                        LOG.warning("Unexpected exception synchronising agent '" + agent.getName() + "': " + e.getMessage(), e);
                    }
                    finally
                    {
                        tryToComplete(agentId, successful);
                    }
                }
                finally
                {
                    unlockAgent(agentId);
                }
            }

            private void setMessagesToProcessing(List<AgentSynchronisationMessage> pendingMessages)
            {
                long timestamp = clock.getCurrentTimeMillis();
                for (AgentSynchronisationMessage message: pendingMessages)
                {
                    message.startProcessing(timestamp);
                }

                agentManager.saveSynchronisationMessages(pendingMessages);
            }

            private boolean sendPendingMessages(Agent agent, List<AgentSynchronisationMessage> pendingMessages)
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
                for (final SynchronisationMessageResult result: results)
                {
                    AgentSynchronisationMessage correspondingMessage = CollectionUtils.find(pendingMessages, new Predicate<AgentSynchronisationMessage>()
                    {
                        public boolean satisfied(AgentSynchronisationMessage agentSynchronisationMessage)
                        {
                            return agentSynchronisationMessage.getId() == result.getMessageId();
                        }
                    });

                    if (correspondingMessage != null)
                    {
                        correspondingMessage.applyResult(result);
                    }
                }

                agentManager.saveSynchronisationMessages(pendingMessages);
            }

            private void cleanupOldCompletedMessages(List<AgentSynchronisationMessage> messages)
            {
                List<AgentSynchronisationMessage> completed = filter(messages, COMPLETED_MESSAGES_PREDICATE);
                if (completed.size() > COMPLETED_MESSAGE_LIMIT)
                {
                    agentManager.dequeueSynchronisationMessages(completed.subList(0, completed.size() - COMPLETED_MESSAGE_LIMIT));
                }
            }
        });
    }

    private void lockAgent(long id)
    {
        allAgentsLock.readLock().lock();
        getAgentLock(id).lock();
    }

    private void unlockAgent(long id)
    {
        getAgentLock(id).unlock();
        allAgentsLock.readLock().unlock();
    }

    private Lock getAgentLock(long id)
    {
        synchronized (agentLocks)
        {
            Lock lock = agentLocks.get(id);
            if (lock == null)
            {
                lock = new ReentrantLock();
                agentLocks.put(id, lock);
            }

            return lock;
        }
    }

    private void tryToComplete(long agentId, boolean successful)
    {
        if (!agentManager.completeSynchronisation(agentId, successful))
        {
            syncAgent(agentId);
        }
    }

    private void handleMessageProcessed(final long agentId, final SynchronisationMessageResult result)
    {
        getExecutorService().submit(new Runnable()
        {
            public void run()
            {
                lockAgent(agentId);
                try
                {
                    AgentSynchronisationMessage message = agentManager.getSynchronisationMessage(result.getMessageId());
                    if (message != null && message.getStatus() == AgentSynchronisationMessage.Status.PROCESSING)
                    {
                        message.applyResult(result);
                        agentManager.saveSynchronisationMessages(Arrays.asList(message));
                        tryToComplete(agentId, true);
                    }
                }
                finally
                {
                    unlockAgent(agentId);
                }
            }
        });
    }
    
    public void handleEvent(Event event)
    {
        if (event instanceof AgentStatusChangeEvent)
        {
            AgentStatusChangeEvent asce = (AgentStatusChangeEvent) event;
            if (asce.getNewStatus() == AgentStatus.SYNCHRONISING)
            {
                syncAgent(asce.getAgent().getId());
            }
        }
        else
        {
            SynchronisationMessageProcessedEvent smpe = (SynchronisationMessageProcessedEvent) event;
            handleMessageProcessed(smpe.getAgentId(), smpe.getResult());
        }
    }

    public Class[] getHandledEvents()
    {
        return new Class[]{AgentStatusChangeEvent.class, SynchronisationMessageProcessedEvent.class};
    }

    public void setEventManager(EventManager eventManager)
    {
        this.eventManager = eventManager;
    }

    public void setClock(Clock clock)
    {
        this.clock = clock;
    }

    public void setCallbackService(CallbackService callbackService)
    {
        this.callbackService = callbackService;
    }

    public static class StatusInPredicate implements Predicate<AgentSynchronisationMessage>
    {
        private Set<AgentSynchronisationMessage.Status> acceptableStatuses;

        public StatusInPredicate(Set<AgentSynchronisationMessage.Status> acceptableStatuses)
        {
            this.acceptableStatuses = acceptableStatuses;
        }

        public boolean satisfied(AgentSynchronisationMessage message)
        {
            return acceptableStatuses.contains(message.getStatus());
        }
    }

    private static class ExtractMessageMapping implements Mapping<AgentSynchronisationMessage, SynchronisationMessage>
    {
        public SynchronisationMessage map(AgentSynchronisationMessage message)
        {
            SynchronisationMessage m = message.getMessage();
            m.setId(message.getId());
            return m;
        }
    }
}
