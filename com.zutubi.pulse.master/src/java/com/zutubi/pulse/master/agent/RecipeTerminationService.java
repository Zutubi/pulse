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

package com.zutubi.pulse.master.agent;

import com.zutubi.events.Event;
import com.zutubi.events.EventListener;
import com.zutubi.events.EventManager;
import com.zutubi.pulse.master.events.build.RecipeTerminateRequestEvent;
import com.zutubi.util.logging.Logger;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Simple service for requesting recipes to terminate.  As this involves a
 * network call to the agent, it is not desirable to do it inline.
 */
public class RecipeTerminationService implements EventListener
{
    private static final Logger LOG = Logger.getLogger(RecipeTerminationService.class);

    private AtomicInteger id = new AtomicInteger(1);
    private ExecutorService executorService;
    private EventManager eventManager;
    private ThreadFactory threadFactory;

    public void init()
    {
        executorService = Executors.newCachedThreadPool(new ThreadFactory()
        {
            public Thread newThread(Runnable r)
            {
                Thread t = threadFactory.newThread(r);
                t.setDaemon(true);
                t.setName("Recipe Termination Service Worker " + id.getAndIncrement());
                return t;
            }
        });

        eventManager.register(this);
    }

    public void handleEvent(final Event e)
    {
        executorService.execute(new Runnable()
        {
            public void run()
            {
                RecipeTerminateRequestEvent rtr = (RecipeTerminateRequestEvent) e;
                try
                {
                    rtr.getService().terminateRecipe(rtr.getRecipeId());
                }
                catch (Exception e)
                {
                    LOG.warning("Unable to terminate recipe '" + rtr.getRecipeId() + "': " + e.getMessage(), e);
                }
            }
        });
    }

    public Class[] getHandledEvents()
    {
        return new Class[] { RecipeTerminateRequestEvent.class };
    }

    public void setEventManager(EventManager eventManager)
    {
        this.eventManager = eventManager;
    }

    public void setThreadFactory(ThreadFactory threadFactory)
    {
        this.threadFactory = threadFactory;
    }
}
