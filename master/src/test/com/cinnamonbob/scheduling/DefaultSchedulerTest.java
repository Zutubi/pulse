package com.cinnamonbob.scheduling;

import junit.framework.*;
import com.cinnamonbob.scheduling.persistence.mock.MockTriggerDao;

/**
 * <class-comment/>
 */
public class DefaultSchedulerTest extends TestCase
{
    private DefaultScheduler scheduler;

    public DefaultSchedulerTest(String testName)
    {
        super(testName);
    }

    public void setUp() throws Exception
    {
        super.setUp();

        // add setup code here.
        scheduler = new DefaultScheduler();
        scheduler.register(ManualTrigger.class, new ManualSchedulerImpl());
        scheduler.setTriggerDao(new MockTriggerDao());
    }

    public void tearDown() throws Exception
    {
        // add tear down code here.

        super.tearDown();
    }

    public void testTaskExecutionOnTrigger()
    {
        // schedule
        ManualTrigger trigger = new ManualTrigger();
        TestTask task = new TestTask();
        scheduler.schedule(trigger, task);

        // test.
        assertFalse(task.isExecuted());
        scheduler.trigger(trigger, task);
        assertTrue(task.isExecuted());

        // unschedule
        task.reset();
        scheduler.unschedule(trigger);

        // test
        assertFalse(task.isExecuted());
        scheduler.trigger(trigger, task);
        assertFalse(task.isExecuted());
    }

    public void testPauseResumeGroup()
    {
        ManualTrigger triggerA = new ManualTrigger("nameA", "group");
        ManualTrigger triggerB = new ManualTrigger("nameB", "group");
        ManualTrigger triggerC = new ManualTrigger("nameC", "group");

        scheduler.schedule(triggerA, new TestTask());
        scheduler.schedule(triggerB, new TestTask());
        scheduler.schedule(triggerC, new TestTask());

        assertEquals(TriggerState.ACTIVE, triggerA.getState());
        assertEquals(TriggerState.ACTIVE, triggerB.getState());
        assertEquals(TriggerState.ACTIVE, triggerC.getState());

        scheduler.pause("group");

        assertEquals(TriggerState.PAUSED, triggerA.getState());
        assertEquals(TriggerState.PAUSED, triggerB.getState());
        assertEquals(TriggerState.PAUSED, triggerC.getState());

        scheduler.resume("group");

        assertEquals(TriggerState.ACTIVE, triggerA.getState());
        assertEquals(TriggerState.ACTIVE, triggerB.getState());
        assertEquals(TriggerState.ACTIVE, triggerC.getState());

    }
}