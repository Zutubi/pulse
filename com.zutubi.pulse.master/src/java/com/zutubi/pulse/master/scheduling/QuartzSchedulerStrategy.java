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

import com.zutubi.util.bean.ObjectFactory;
import com.zutubi.util.logging.Logger;
import org.quartz.*;

import static com.zutubi.pulse.master.scheduling.QuartzTaskCallbackJob.SOURCE_PROP;

/**
 * The QuartzSchedulerStrategy is an implementation of the SchedulerStrategy
 * interface backed by the quartz scheduler.
 *
 * Any triggers scheduled with this scheduler are passed through to the underlying
 * quartz scheduler.  When those quartz triggers fire, they call the
 * {@link com.zutubi.pulse.master.scheduling.QuartzTaskCallbackJob} which converts
 * the callback into a local trigger.
 */
public abstract class QuartzSchedulerStrategy implements SchedulerStrategy
{
    private static final Logger LOG = Logger.getLogger(QuartzSchedulerStrategy.class);

    protected static final String CALLBACK_JOB_NAME = "cron.trigger.job.name";

    protected static final String CALLBACK_JOB_GROUP = "cron.trigger.job.group";

    private org.quartz.Scheduler quartzScheduler;
    private ObjectFactory objectFactory;

    public void setQuartzScheduler(org.quartz.Scheduler quartzScheduler)
    {
        this.quartzScheduler = quartzScheduler;
    }

    public org.quartz.Scheduler getQuartzScheduler()
    {
        return quartzScheduler;
    }

    public void pause(Trigger trigger) throws SchedulingException
    {
        try
        {
            // if trigger is not scheduled, then schedule it first before pausing.
            TriggerKey triggerKey = new TriggerKey(trigger.getName(), trigger.getGroup());
            org.quartz.Trigger t = getQuartzScheduler().getTrigger(triggerKey);
            if (t == null)
            {
                schedule(trigger);
            }
            getQuartzScheduler().pauseTrigger(triggerKey);
        }
        catch (SchedulerException e)
        {
            throw new SchedulingException(e);
        }
    }

    public void resume(Trigger trigger) throws SchedulingException
    {
        try
        {
            getQuartzScheduler().resumeTrigger(new TriggerKey(trigger.getName(), trigger.getGroup()));
        }
        catch (SchedulerException e)
        {
            throw new SchedulingException(e);
        }
    }

    public void stop(boolean force)
    {
        try
        {
            // Quartz scheduler stop takes an argument indicating if we
            // should wait for scheduled tasks to complete first (hence the
            // inversion of force).
            getQuartzScheduler().shutdown(!force);
        }
        catch (SchedulerException e)
        {
            LOG.severe("Unable to shutdown Quartz scheduler", e);
        }
    }

    public void unschedule(Trigger trigger) throws SchedulingException
    {
        try
        {
            getQuartzScheduler().unscheduleJob(new TriggerKey(trigger.getName(), trigger.getGroup()));
        }
        catch (SchedulerException e)
        {
            throw new SchedulingException(e);
        }
    }

    protected void ensureCallbackRegistered() throws SchedulerException
    {
        JobKey jobKey = new JobKey(CALLBACK_JOB_NAME, CALLBACK_JOB_GROUP);
        JobDetail existingJob = getQuartzScheduler().getJobDetail(jobKey);
        if (existingJob == null)
        {
            // register the job detail once only.
            JobDetail detail = JobBuilder.newJob(QuartzTaskCallbackJob.class)
                    .withIdentity(jobKey)
                    .storeDurably()
                    .build();
            getQuartzScheduler().addJob(detail, true);
        }
    }

    public void init(Trigger trigger) throws SchedulingException
    {
        if (trigger.isScheduled())
        {
            schedule(trigger);
            if(trigger.isPaused())
            {
                pause(trigger);
            }
        }
    }

    public void schedule(Trigger trigger) throws SchedulingException
    {
        try
        {
            ensureCallbackRegistered();

            org.quartz.Trigger quartzTrigger = createTriggerBuilder(trigger).build();

            QuartzTaskCallbackTriggerSource source = objectFactory.buildBean(QuartzTaskCallbackTriggerSource.class, trigger);
            quartzTrigger.getJobDataMap().put(SOURCE_PROP, source);

            getQuartzScheduler().scheduleJob(quartzTrigger);
        }
        catch (SchedulerException e)
        {
            throw new SchedulingException(e);
        }
    }

    public void setTriggerHandler(TriggerHandler handler)
    {
        // noop.
    }

    public void setObjectFactory(ObjectFactory objectFactory)
    {
        this.objectFactory = objectFactory;
    }

    protected TriggerBuilder createTriggerBuilder(Trigger trigger) throws SchedulingException
    {
        return TriggerBuilder.newTrigger()
                .withIdentity(trigger.getName(), trigger.getGroup())
                .withSchedule(createScheduleBuilder(trigger))
                .forJob(CALLBACK_JOB_NAME, CALLBACK_JOB_GROUP);
    }

    protected abstract ScheduleBuilder createScheduleBuilder(Trigger trigger) throws SchedulingException;
}
