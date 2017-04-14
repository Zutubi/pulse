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

package com.zutubi.pulse.master.scheduling.quartz;

import com.zutubi.events.EventManager;
import com.zutubi.pulse.master.events.build.RecipeTimeoutEvent;
import com.zutubi.util.logging.Logger;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

/**
 * This job is used to trigger a recipe timeout event.
 *
 * It is scheduled at the start of a build and will generate a recipe timeout event is it is not unscheduled
 * before its time is up.
 */
public class TimeoutRecipeJob implements Job
{
    private static final Logger LOG = Logger.getLogger(TimeoutRecipeJob.class);

    /**
     * The job data map parameter used to access the build id this job relates.
     */
    public static final String PARAM_BUILD_ID = "BUILD_ID";
    /**
     * The job data map parameter used to access the recipe id this job relates.
     */
    public static final String PARAM_RECIPE_ID = "RECIPE_ID";

    private EventManager eventManager;

    public void execute(JobExecutionContext context) throws JobExecutionException
    {
        long buildId = (Long) context.getTrigger().getJobDataMap().get(PARAM_BUILD_ID);
        long recipeId = (Long) context.getTrigger().getJobDataMap().get(PARAM_RECIPE_ID);
        LOG.debug("Timeout job fired for build " + buildId + ", recipe " + recipeId);
        eventManager.publish(new RecipeTimeoutEvent(this, buildId, recipeId));
    }

    public void setEventManager(EventManager eventManager)
    {
        this.eventManager = eventManager;
    }
}
