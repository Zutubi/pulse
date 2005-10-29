package com.cinnamonbob.bootstrap.quartz;

import com.cinnamonbob.bootstrap.ComponentContext;
import org.quartz.Job;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.simpl.SimpleJobFactory;
import org.quartz.spi.TriggerFiredBundle;

import java.util.logging.Logger;

/**
 * Use spring to autowire jobs are they are created.
 *
 */
public class SpringJobFactory extends SimpleJobFactory
{
    private static final Logger LOG = Logger.getLogger(SpringJobFactory.class.getName());

    public Job newJob(TriggerFiredBundle bundle) throws SchedulerException
    {
        Job job = super.newJob(bundle);

        ComponentContext.autowire(job);

        return job;
    }

    // setup: install this job factory in the scheduler. Would be nice if this
    // could be achieved via a setter on the SchedulerFactoryBean. However, currently
    // this is not available.

    private Scheduler scheduler;

    public void setScheduler(Scheduler scheduler)
    {
        this.scheduler = scheduler;
    }

    public void initJobFactory() throws SchedulerException
    {
        scheduler.setJobFactory(this);
    }
}
