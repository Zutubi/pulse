package com.zutubi.pulse.master.scheduling;

import com.zutubi.pulse.master.scheduling.quartz.TriggerAdapter;
import com.zutubi.util.bean.WiringObjectFactory;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.SchedulerException;
import org.quartz.SchedulerFactory;
import org.quartz.impl.StdSchedulerFactory;
import org.quartz.spi.JobFactory;
import org.quartz.spi.TriggerFiredBundle;

import static com.zutubi.pulse.master.scheduling.QuartzSchedulerStrategy.CALLBACK_JOB_GROUP;
import static com.zutubi.pulse.master.scheduling.QuartzSchedulerStrategy.CALLBACK_JOB_NAME;

public class CronSchedulerStrategyTest extends SchedulerStrategyTestBase
{
    private org.quartz.Scheduler quartzScheduler = null;
    private TestTriggerHandler triggerHandler = null;

    public void setUp() throws Exception
    {
        super.setUp();

        WiringObjectFactory objectFactory = new WiringObjectFactory();

        triggerHandler = new TestTriggerHandler();

        SchedulerFactory schedFact = new StdSchedulerFactory();
        quartzScheduler = schedFact.getScheduler();
        quartzScheduler.setJobFactory(new JobFactory()
        {
            public Job newJob(TriggerFiredBundle bundle) throws SchedulerException
            {
                QuartzTaskCallbackJob job = new QuartzTaskCallbackJob();
                job.setTriggerHandler(triggerHandler);
                return job;
            }
        });
        quartzScheduler.start();
        scheduler = new CronSchedulerStrategy();
        ((QuartzSchedulerStrategy)scheduler).setObjectFactory(objectFactory);
        ((QuartzSchedulerStrategy)scheduler).setQuartzScheduler(quartzScheduler);

        objectFactory.initProperties(this);
    }

    public void tearDown() throws Exception
    {
        quartzScheduler.shutdown();
        super.tearDown();
    }

    @Override
    public void testTaskExecutedOnTrigger() throws SchedulingException
    {
        super.testTaskExecutedOnTrigger();
    }

    @Override
    public void testPauseTrigger() throws SchedulingException
    {
        super.testPauseTrigger();
    }

    @Override
    public void testTriggerCount() throws SchedulingException
    {
        super.testTriggerCount();
    }

    @Override
    protected TestTriggerHandler getHandler()
    {
        return triggerHandler;
    }

    protected void activateTrigger(Trigger trigger) throws SchedulingException
    {
        try
        {
            String name = trigger.getName();
            String group = trigger.getGroup();
            org.quartz.Trigger t = quartzScheduler.getTrigger(name, group);
            if (t != null)
            {
                // if the quartz trigger is currently paused, then we should not trigger since this
                // is not what quartz itself would do. Remember, we are just trying to imitate quartz
                // in a controlled fashion.
                int state = quartzScheduler.getTriggerState(name, group);
                if (state == org.quartz.Trigger.STATE_PAUSED)
                {
                    return;
                }

                // we need to ensure that the quartz threads have had a chance to trigger
                // the job, so register a callback and then yeild until it is received.
                final boolean[] triggered = new boolean[]{false};
                TriggerAdapter globalTriggerListener = new TriggerAdapter()
                {
                    public void triggerComplete(org.quartz.Trigger trigger, JobExecutionContext context, int triggerInstructionCode)
                    {
                        triggered[0] = true;
                    }
                };
                
                quartzScheduler.addGlobalTriggerListener(globalTriggerListener);

                // manually trigger the quartz callback job with the triggers details.
                quartzScheduler.triggerJob(CALLBACK_JOB_NAME, CALLBACK_JOB_GROUP, t.getJobDataMap());

                while (!triggered[0])
                {
                    Thread.yield();
                }
            }
        }
        catch (SchedulerException e)
        {
            // trying to activate a job that is not registered?
            // well, that's because its not scheduled....
            throw new SchedulingException(e);
        }
    }

    protected Trigger createTrigger()
    {
        // create a new quartz trigger. Ideally, this trigger would not possibly trigger
        // during the course of this test case since we are 'manually' handling the triggering
        // via the activateTrigger method.
        return new CronTrigger("0 0 0 ? * WED", getName());
    }
}
