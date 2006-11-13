package com.zutubi.pulse.scheduling;

import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;
import com.zutubi.pulse.scheduling.quartz.TriggerAdapter;

/**
 * <class-comment/>
 */
public class CronSchedulerStrategyTest extends SchedulerStrategyTestBase
{
    private org.quartz.Scheduler quartzScheduler = null;

    public CronSchedulerStrategyTest(String testName)
    {
        super(testName);
    }

    public void setUp() throws Exception
    {
        super.setUp();

        // add setup code here.
        SchedulerFactory schedFact = new StdSchedulerFactory();
        quartzScheduler = schedFact.getScheduler();
        quartzScheduler.setJobFactory(new QuartzTaskJobFactory(triggerHandler));
        quartzScheduler.start();
        scheduler = new CronSchedulerStrategy();
        ((QuartzSchedulerStrategy)scheduler).setQuartzScheduler(quartzScheduler);
    }

    public void tearDown() throws Exception
    {
        // add tear down code here.
        scheduler = null;
        quartzScheduler.shutdown();
        quartzScheduler = null;

        super.tearDown();
    }

    protected void activateTrigger(Trigger trigger) throws SchedulingException
    {
        try
        {
            org.quartz.Trigger t = quartzScheduler.getTrigger(trigger.getName(), trigger.getGroup());
            if (t != null)
            {
                // if the quartz trigger is currently paused, then we should not trigger since this
                // is not what quartz itself would do. Remember, we are just trying to imitate quartz
                // in a controlled fashion.
                int state = quartzScheduler.getTriggerState(t.getName(), t.getGroup());
                if (state == org.quartz.Trigger.STATE_PAUSED)
                {
                    return;
                }

                // we need to ensure that the quartz threads have had a chance to trigger
                // the job, so register a callback and then yeild until it is received.
                final boolean[] triggered = new boolean[]{false};
                quartzScheduler.addGlobalTriggerListener(new TriggerAdapter()
                {
                    public void triggerComplete(org.quartz.Trigger trigger, JobExecutionContext context, int triggerInstructionCode)
                    {
                        triggered[0] = true;
                    }
                });

                // manually trigger the quartz callback job.
                JobDataMap map = quartzScheduler.getJobDetail(QuartzSchedulerStrategy.CALLBACK_JOB_NAME, QuartzSchedulerStrategy.CALLBACK_JOB_GROUP).getJobDataMap();
                map.clear();
                map.putAll(t.getJobDataMap());
                quartzScheduler.triggerJob("cron.trigger.job.name", "cron.trigger.job.group");

                while (!triggered[0])
                {
                    Thread.yield();
                }
            }
        }
        catch (SchedulerException e)
        {
            // trying to activate a job that is not registered? well, thats because its not scheduled....
            throw new SchedulingException(e);
        }
    }

    protected Trigger createTrigger()
    {
        // create a new quartz trigger. Ideally, this trigger would not possibly trigger
        // during the course of this test case since we are 'manually' handling the triggering
        // via the activateTrigger method.
        return new CronTrigger("0 0 12 ? * WED", "default");
    }
}