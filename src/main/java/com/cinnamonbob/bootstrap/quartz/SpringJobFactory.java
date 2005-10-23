package com.cinnamonbob.bootstrap.quartz;

import com.cinnamonbob.spring.SpringAutowireSupport;
import org.quartz.Job;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.simpl.SimpleJobFactory;
import org.quartz.spi.TriggerFiredBundle;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.util.logging.Logger;

/**
 * Use spring to autowire jobs are they are created.
 *
 */
public class SpringJobFactory extends SimpleJobFactory implements ApplicationContextAware
{
    private static final Logger LOG = Logger.getLogger(SpringJobFactory.class.getName());

    private SpringAutowireSupport autowireSupport = new SpringAutowireSupport();

    public Job newJob(TriggerFiredBundle bundle) throws SchedulerException
    {
        Job job = super.newJob(bundle);

        autowireSupport.autoWireBean(job);

        return job;
    }

    public void setApplicationContext(ApplicationContext context) throws BeansException
    {
        autowireSupport.setApplicationContext(context);
    }

    /**
     * Sets the autowiring strategy
     *
     * @param autowireStrategy
     */
    public void setAutowireStrategy(int autowireStrategy)
    {
        autowireSupport.setAutowireStrategy(autowireStrategy);
    }

    public int getAutowireStrategy()
    {
        return autowireSupport.getAutowireStrategy();
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
