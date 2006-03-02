package com.cinnamonbob.bootstrap.quartz;

import com.cinnamonbob.core.ObjectFactory;
import com.cinnamonbob.util.logging.Logger;
import org.quartz.Job;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.spi.JobFactory;
import org.quartz.spi.TriggerFiredBundle;

/**
 * Use spring to autowire jobs are they are created.
 *
 */
public class SpringJobFactory implements JobFactory
{
    private static final Logger LOG = Logger.getLogger(SpringJobFactory.class);

    private ObjectFactory objectFactory;

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

    /**
     * Set the object factory to be used by this job factory implementation.
     *
     * @param objectFactory
     */
    public void setObjectFactory(ObjectFactory objectFactory)
    {
        this.objectFactory = objectFactory;
    }
}
