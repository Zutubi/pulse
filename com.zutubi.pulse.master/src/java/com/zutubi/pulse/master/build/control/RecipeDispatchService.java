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

package com.zutubi.pulse.master.build.control;

import com.zutubi.events.EventManager;
import com.zutubi.pulse.core.events.RecipeErrorEvent;
import com.zutubi.pulse.master.events.build.RecipeAssignedEvent;
import com.zutubi.pulse.master.events.build.RecipeDispatchedEvent;
import com.zutubi.pulse.servercore.util.background.BackgroundServiceSupport;
import static com.zutubi.util.StringUtils.safeToString;
import com.zutubi.util.logging.Logger;

import java.util.logging.Level;

/**
 * A simple service to farm assigned recipes out to the agents.  This is done
 * in a service as it requires a (possibly-slow) network call to the agent.
 */
public class RecipeDispatchService extends BackgroundServiceSupport
{
    private static final Logger LOG = Logger.getLogger(RecipeDispatchService.class);

    private static final String PROPERTY_POOL_SIZE = "pulse.recipe.dispatch.pool.size";
    private static final int DEFAULT_POOL_SIZE = 5;

    private EventManager eventManager;

    public RecipeDispatchService()
    {
        super("Recipe Dispatch", getPoolSize());
    }

    private static int getPoolSize()
    {
        try
        {
            return Integer.parseInt(System.getProperty(PROPERTY_POOL_SIZE, Integer.toString(DEFAULT_POOL_SIZE)));
        }
        catch (NumberFormatException e)
        {
            LOG.warning(e);
            return DEFAULT_POOL_SIZE;
        }
    }

    public void dispatch(final RecipeAssignedEvent assignment)
    {
        getExecutorService().execute(new Runnable()
        {
            public void run()
            {
                if (LOG.isLoggable(Level.FINER))
                {
                    LOG.finer("Recipe dispatch service: dispatching " + safeToString(assignment));
                }

                try
                {
                    assignment.getAgent().getService().build(assignment.getRequest());
                    eventManager.publish(new RecipeDispatchedEvent(this, assignment.getBuildId(), assignment.getRecipeId(), assignment.getAgent()));
                }
                catch (Exception e)
                {
                    LOG.warning("Unable to dispatch recipe: " + e.getMessage(), e);
                    eventManager.publish(new RecipeErrorEvent(this, assignment.getBuildId(), assignment.getRecipeId(), "Unable to dispatch recipe: " + e.getMessage(), true));
                }
                finally
                {
                    if (LOG.isLoggable(Level.FINER))
                    {
                        LOG.finer("Recipe dispatch service: dispatched " + safeToString(assignment));
                    }
                }
            }
        });
    }

    public void setEventManager(EventManager eventManager)
    {
        this.eventManager = eventManager;
    }
}
