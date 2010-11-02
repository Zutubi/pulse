package com.zutubi.pulse.master.scheduling;

import com.zutubi.pulse.core.spring.SpringComponentContext;
import com.zutubi.util.bean.ObjectFactory;
import org.quartz.Job;
import org.quartz.SchedulerException;
import org.quartz.spi.JobFactory;
import org.quartz.spi.TriggerFiredBundle;

/**
 * This implementation of the {@link JobFactory} interface
 * ensures that all of the jobs created for Quartz are correctly
 * wired.
 */
public class QuartzTaskJobFactory implements JobFactory
{
    private ObjectFactory objectFactory;

    public Job newJob(TriggerFiredBundle bundle) throws SchedulerException
    {
        Class jobClass = bundle.getJobDetail().getJobClass();
        if (jobClass == QuartzTaskCallbackJob.class)
        {
            // The QuartzTaskCallbackJob is a special case because it is
            // wrapped by a transaction proxy.  If we request the bean
            // by class, then the unwrapped version is returned.
            return SpringComponentContext.getBean("quartzTaskCallbackJob");
        }
        return (Job) objectFactory.buildBean(jobClass);
    }

    public void setObjectFactory(ObjectFactory objectFactory)
    {
        this.objectFactory = objectFactory;
    }
}
