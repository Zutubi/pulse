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

package com.zutubi.pulse.servercore;

import com.zutubi.pulse.core.RecipeProcessor;
import com.zutubi.pulse.core.RecipeRequest;
import com.zutubi.pulse.servercore.services.HostStatus;
import com.zutubi.pulse.servercore.util.background.BackgroundServiceSupport;
import com.zutubi.util.bean.ObjectFactory;
import com.zutubi.util.logging.Logger;

import java.util.HashMap;
import java.util.Map;

/**
 * A service for executing recipes on a machine.  Manages a pool of threads to
 * run recipes for different agents.
 */
public class ServerRecipeService extends BackgroundServiceSupport
{
    private static final Logger LOG = Logger.getLogger(ServerRecipeService.class);

    private final Map<Long, RecipeProcessor> agentHandleToProcessorMap = new HashMap<Long, RecipeProcessor>();
    private final Map<Long, Long> agentHandleToRecipeIdMap = new HashMap<Long, Long>();

    private ObjectFactory objectFactory;

    public ServerRecipeService()
    {
        super("Server Recipe Service");
    }

    public void processRecipe(final long agentHandle, final RecipeRequest request, final RecipeRunner runner)
    {
        synchronized (agentHandleToProcessorMap)
        {
            final RecipeProcessor processor = getProcessorForAgent(agentHandle);

            getExecutorService().submit(new Runnable()
            {
                public void run()
                {
                    try
                    {
                        synchronized (agentHandleToRecipeIdMap)
                        {
                            agentHandleToRecipeIdMap.put(agentHandle, request.getId());
                        }

                        runner.runRecipe(request, processor);
                    }
                    finally
                    {
                        synchronized (agentHandleToRecipeIdMap)
                        {
                            agentHandleToRecipeIdMap.put(agentHandle, HostStatus.NO_RECIPE);
                        }
                    }
                }
            });
        }
    }

    private RecipeProcessor getProcessorForAgent(long agentHandle)
    {
        RecipeProcessor processor = agentHandleToProcessorMap.get(agentHandle);
        if (processor == null)
        {
            synchronized (agentHandleToRecipeIdMap)
            {
                agentHandleToRecipeIdMap.put(agentHandle, HostStatus.NO_RECIPE);
            }

            processor = objectFactory.buildBean(RecipeProcessor.class);
            agentHandleToProcessorMap.put(agentHandle, processor);
        }

        return processor;
    }

    public Map<Long, Long> getBuildingRecipes()
    {
        Map<Long, Long> building = new HashMap<Long, Long>();
        synchronized (agentHandleToProcessorMap)
        {
            building.putAll(agentHandleToRecipeIdMap);
        }

        return building;
    }

    public void terminateRecipe(long agentHandle, long id)
    {
        RecipeProcessor recipeProcessor;
        synchronized (agentHandleToProcessorMap)
        {
            recipeProcessor = getProcessorForAgent(agentHandle);
        }

        try
        {
            recipeProcessor.terminateRecipe(id);
        }
        catch (InterruptedException e)
        {
            LOG.warning("Interrupted while terminating recipe", e);
        }
    }

    public void setObjectFactory(ObjectFactory objectFactory)
    {
        this.objectFactory = objectFactory;
    }
}
