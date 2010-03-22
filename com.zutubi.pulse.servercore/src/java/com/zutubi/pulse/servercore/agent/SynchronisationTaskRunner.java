package com.zutubi.pulse.servercore.agent;

import com.zutubi.util.CollectionUtils;
import com.zutubi.util.Mapping;

import java.util.List;

/**
 * A service that executes {@link com.zutubi.pulse.servercore.agent.SynchronisationTask}s.
 * Messages come in, are converted to tasks and executed, and results come out.
 */
public class SynchronisationTaskRunner
{
    private SynchronisationTaskFactory synchronisationTaskFactory;

    /**
     * Process all of the given messages by converting them to tasks and
     * executing them, returning corresponding results for each message.
     *
     * @param messages the messages to process
     * @return a result for each message, in the order of the messages
     */
    public List<SynchronisationMessageResult> synchronise(List<SynchronisationMessage> messages)
    {
        return CollectionUtils.map(messages, new Mapping<SynchronisationMessage, SynchronisationMessageResult>()
        {
            public SynchronisationMessageResult map(SynchronisationMessage message)
            {
                return execute(message);
            }
        });
    }

    private SynchronisationMessageResult execute(SynchronisationMessage message)
    {
        try
        {
            SynchronisationTask task = synchronisationTaskFactory.fromMessage(message);
            task.execute();
            return new SynchronisationMessageResult();
        }
        catch (Exception e)
        {
            return new SynchronisationMessageResult(e);
        }
    }

    public void setSynchronisationTaskFactory(SynchronisationTaskFactory synchronisationTaskFactory)
    {
        this.synchronisationTaskFactory = synchronisationTaskFactory;
    }
}
