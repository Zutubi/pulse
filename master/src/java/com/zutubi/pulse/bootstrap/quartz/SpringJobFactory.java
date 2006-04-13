/********************************************************************************
 @COPYRIGHT@
 ********************************************************************************/
package com.zutubi.pulse.bootstrap.quartz;

import com.zutubi.pulse.core.ObjectFactory;
import com.zutubi.pulse.util.logging.Logger;
import org.quartz.Job;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.spi.JobFactory;
import org.quartz.spi.TriggerFiredBundle;

/**
 * Wire the configured object factory to handle job creation.
 *
 */
public class SpringJobFactory implements JobFactory
{
    private static final Logger LOG = Logger.getLogger(SpringJobFactory.class);

    /**
     * The systems object factory, which will be used to create job instances.
     */
    private ObjectFactory objectFactory;

    /**
     * The quartz scheduler into which we are installing this custom JobFactory.
     */
    private Scheduler scheduler;

    /**
     * Create a new job instance based on the details in the TriggerFiredBundle.
     *
     * @param bundle
     *
     * @return a new job instance created using the configured ObjectFactory
     *
     * @throws SchedulerException
     */
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

    /**
     * Required resource.
     * 
     * @param scheduler
     */
    public void setScheduler(Scheduler scheduler)
    {
        this.scheduler = scheduler;
    }

    /**
     * Register this job factory with the scheduler so that it is used to create
     * the quartz jobs.
     *
     * @throws SchedulerException
     */
    public void init() throws SchedulerException
    {
        // setup: install this job factory in the scheduler. Would be nice if this
        // could be achieved via a setter on the SchedulerFactoryBean. However, currently
        // this is not available.
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
