package com.zutubi.pulse.servercore.agent;

import com.zutubi.events.EventManager;
import com.zutubi.pulse.servercore.events.SynchronisationMessageProcessedEvent;
import com.zutubi.pulse.servercore.util.background.BackgroundServiceSupport;

import java.util.LinkedList;
import java.util.List;

/**
 * A service that executes {@link com.zutubi.pulse.servercore.agent.SynchronisationTask}s.
 * Messages come in, are converted to tasks and executed, and results come out.
 */
public class SynchronisationTaskRunnerService extends BackgroundServiceSupport
{
    private SynchronisationTaskFactory synchronisationTaskFactory;
    private EventManager eventManager;

    public SynchronisationTaskRunnerService()
    {
        super("Synchronisation Task Runner Service", 1);
    }

    /**
     * Process all of the given messages by converting them to tasks and
     * executing them, returning results for each synchronous message.
     * Asynchronous results are returned independently at a later time.
     *
     * @param agentId  id of the agent the messages are for
     * @param messages the messages to process
     * @return a result for each synchronous message
     */
    public List<SynchronisationMessageResult> synchronise(long agentId, List<SynchronisationMessage> messages)
    {
        List<SynchronisationMessageResult> results = new LinkedList<SynchronisationMessageResult>();
        for (SynchronisationMessage message: messages)
        {
            if (message.getType().isSynchronous())
            {
                results.add(execute(message));
            }
            else
            {
                executeAsync(agentId, message);
            }
        }
        
        return results;
    }

    private void executeAsync(final long agentId, final SynchronisationMessage message)
    {
        getExecutorService().submit(new Runnable()
        {
            public void run()
            {
                SynchronisationMessageResult result = execute(message);
                eventManager.publish(new SynchronisationMessageProcessedEvent(this, agentId, result));
            }
        });
    }

    private SynchronisationMessageResult execute(SynchronisationMessage message)
    {
        try
        {
            SynchronisationTask task = synchronisationTaskFactory.fromMessage(message);
            task.execute();
            return new SynchronisationMessageResult(message.getId());
        }
        catch (Exception e)
        {
            return new SynchronisationMessageResult(message.getId(), e);
        }
    }

    public void setSynchronisationTaskFactory(SynchronisationTaskFactory synchronisationTaskFactory)
    {
        this.synchronisationTaskFactory = synchronisationTaskFactory;
    }

    public void setEventManager(EventManager eventManager)
    {
        this.eventManager = eventManager;
    }
}
