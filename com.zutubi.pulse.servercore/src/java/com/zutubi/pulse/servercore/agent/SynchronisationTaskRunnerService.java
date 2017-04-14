/* Copyright 2017 Zutubi Pty Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
