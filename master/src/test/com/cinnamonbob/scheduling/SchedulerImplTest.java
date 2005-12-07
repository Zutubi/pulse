package com.cinnamonbob.scheduling;

import junit.framework.TestCase;

/**
 * <class-comment/>
 */
public abstract class SchedulerImplTest extends TestCase
{
    protected SchedulerImpl scheduler = null;

    public SchedulerImplTest(String testName)
    {
        super(testName);
    }

    public void setUp() throws Exception
    {
        super.setUp();

        // add setup code here.
    }

    public void tearDown() throws Exception
    {
        // add tear down code here.

        super.tearDown();
    }

    public void testTaskExecutedOnTrigger() throws SchedulingException
    {
        Trigger trigger = createTrigger();
        TestTask task = new TestTask("testName", "testGroup");
        scheduler.schedule(trigger, task);

        // test.
        assertFalse(task.isExecuted());
        activateTrigger(trigger, task);
        assertTrue(task.isExecuted());

        // unschedule
        task.reset();
        scheduler.unschedule(trigger);

        // test
        assertFalse(task.isExecuted());
        activateTrigger(trigger, task);
        assertFalse(task.isExecuted());
    }

    public void testTriggerStates() throws SchedulingException
    {
        Trigger trigger = createTrigger();
        assertEquals(TriggerState.NONE, trigger.getState());
        TestTask task = new TestTask("testName", "testGroup");
        scheduler.schedule(trigger, task);
        assertEquals(TriggerState.ACTIVE, trigger.getState());
        scheduler.pause(trigger);
        assertEquals(TriggerState.PAUSED, trigger.getState());
        scheduler.resume(trigger);
        assertEquals(TriggerState.ACTIVE, trigger.getState());
        scheduler.unschedule(trigger);
        assertEquals(TriggerState.NONE, trigger.getState());
    }

    public void testPauseTrigger() throws SchedulingException
    {
        Trigger trigger = createTrigger();
        TestTask task = new TestTask("testName", "testGroup");
        scheduler.schedule(trigger, task);
        assertEquals(0, trigger.getTriggerCount());
        activateTrigger(trigger, task);
        assertEquals(1, trigger.getTriggerCount());
        scheduler.pause(trigger);
        activateTrigger(trigger, task);
        assertEquals(1, trigger.getTriggerCount());
        scheduler.resume(trigger);
        activateTrigger(trigger, task);
        assertEquals(2, trigger.getTriggerCount());
    }

    public void testTriggerCount() throws SchedulingException
    {
        // schedule
        Trigger trigger = createTrigger();
        TestTask task = new TestTask("testName", "testGroup");
        scheduler.schedule(trigger, task);
        assertEquals(0, trigger.getTriggerCount());
        activateTrigger(trigger, task);
        assertEquals(1, trigger.getTriggerCount());
        activateTrigger(trigger, task);
        assertEquals(2, trigger.getTriggerCount());
        scheduler.unschedule(trigger);
        activateTrigger(trigger, task);
        assertEquals(2, trigger.getTriggerCount());
    }

    protected abstract Trigger createTrigger();
    protected abstract void activateTrigger(Trigger trigger, Task task) throws SchedulingException;
}