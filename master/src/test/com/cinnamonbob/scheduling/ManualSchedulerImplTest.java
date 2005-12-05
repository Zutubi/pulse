package com.cinnamonbob.scheduling;

/**
 * <class-comment/>
 */
public class ManualSchedulerImplTest extends SchedulerImplTest
{
    public ManualSchedulerImplTest(String testName)
    {
        super(testName);
    }

    public void setUp() throws Exception
    {
        super.setUp();

        // add setup code here.
        scheduler = new ManualSchedulerImpl();
    }

    public void tearDown() throws Exception
    {
        // add tear down code here.
        scheduler = null;

        super.tearDown();
    }

    protected void activateTrigger(Trigger trigger, Task task)
    {
        scheduler.trigger(trigger, task);
    }

    protected Trigger createTrigger()
    {
        return new ManualTrigger();
    }
}