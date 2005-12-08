package com.cinnamonbob.scheduling;

import org.quartz.*;
import org.quartz.spi.JobFactory;
import org.quartz.spi.TriggerFiredBundle;
import org.quartz.impl.StdSchedulerFactory;

/**
 * <class-comment/>
 */
public class CronSchedulerStrategyTest extends BaseSchedulerStrategyTest implements JobFactory
{
    private Scheduler quartzScheduler = null;

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
        quartzScheduler.setJobFactory(this);
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

    protected void activateTrigger(Trigger trigger, Task task) throws SchedulingException
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

    public Job newJob(TriggerFiredBundle bundle) throws SchedulerException
    {
        QuartzTaskCallbackJob task = new QuartzTaskCallbackJob();
        task.setTriggerHandler(new DefaultTriggerHandler());
        return task;
    }
}

class TriggerAdapter implements TriggerListener
{
    public String getName()
    {
        return "TriggerAdapter";
    }

    public void triggerComplete(org.quartz.Trigger trigger, JobExecutionContext context, int triggerInstructionCode)
    {

    }

    public void triggerFired(org.quartz.Trigger trigger, JobExecutionContext context)
    {

    }

    public void triggerMisfired(org.quartz.Trigger trigger)
    {

    }

    public boolean vetoJobExecution(org.quartz.Trigger trigger, JobExecutionContext context)
    {
        return false;
    }
}