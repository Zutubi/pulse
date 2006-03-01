package com.cinnamonbob.bootstrap.quartz;

import com.cinnamonbob.bootstrap.ComponentContext;
import com.cinnamonbob.util.logging.Logger;
import com.cinnamonbob.spring.SpringObjectFactory;
import org.quartz.Job;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.JobDetail;
import org.quartz.simpl.SimpleJobFactory;
import org.quartz.spi.TriggerFiredBundle;
import org.quartz.spi.JobFactory;

/**
 * Use spring to autowire jobs are they are created.
 *
 */
public class SpringJobFactory implements JobFactory
{
    private static final Logger LOG = Logger.getLogger(SpringJobFactory.class);

    //TODO: need a better way to install the object factory.
    private SpringObjectFactory objectFactory = new SpringObjectFactory();

    public Job newJob(TriggerFiredBundle bundle) throws SchedulerException
    {
        JobDetail jobDetail = bundle.getJobDetail();
        Class jobClass = jobDetail.getJobClass();

        try
        {
            return objectFactory.buildBean(jobClass);
        }
        catch (Exception e)
        {
            throw new SchedulerException(e);
        }
    }

    private Scheduler scheduler;

    public void setScheduler(Scheduler scheduler)
    {
        this.scheduler = scheduler;
    }

    // setup: install this job factory in the scheduler. Would be nice if this
    // could be achieved via a setter on the SchedulerFactoryBean. However, currently
    // this is not available.
    public void init() throws SchedulerException
    {
        scheduler.setJobFactory(this);
    }
}
