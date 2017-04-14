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

package com.zutubi.pulse.master.scheduling;

import com.zutubi.util.logging.Logger;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

/**
 * All of the triggers scheduled within the Quartz scheduler
 * trigger this job.  This job then examines the triggers
 * job data map and uses its contents to determine which
 * pulse trigger is associated with the callback. 
 */
public class QuartzTaskCallbackJob implements Job
{
    private static final Logger LOG = Logger.getLogger(QuartzTaskCallbackJob.class);

    public static final String SOURCE_PROP = "source";

    private TriggerHandler triggerHandler;

    public void execute(JobExecutionContext context) throws JobExecutionException
    {
        LOG.entering();

        // notify schedule manager that this trigger has been activated.
        JobDataMap data = context.getMergedJobDataMap();
        try
        {
            QuartzTaskCallbackTriggerSource source = (QuartzTaskCallbackTriggerSource) data.get(SOURCE_PROP);
            triggerHandler.fire(source.getTrigger());
        }
        catch (SchedulingException e)
        {
            throw new JobExecutionException(e);
        }

        LOG.exiting();
    }

    public void setTriggerHandler(TriggerHandler triggerHandler)
    {
        this.triggerHandler = triggerHandler;
    }
}
