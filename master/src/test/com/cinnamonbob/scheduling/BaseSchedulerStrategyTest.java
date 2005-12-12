package com.cinnamonbob.scheduling;

import junit.framework.TestCase;

/**
 * <class-comment/>
 */
public abstract class BaseSchedulerStrategyTest extends TestCase
{
    protected SchedulerStrategy scheduler = null;
    protected TestTriggerHandler triggerHandler = null;

    public BaseSchedulerStrategyTest(String testName)
    {
        super(testName);
    }

    public void setUp() throws Exception
    {
        super.setUp();
        triggerHandler = new TestTriggerHandler();
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
        NoopTask task = new NoopTask("testName", "testGroup");
        scheduler.schedule(trigger);

        // test.
        assertFalse(triggerHandler.wasTriggered());
        activateTrigger(trigger);
        assertTrue(triggerHandler.wasTriggered());

        // unschedule
        triggerHandler.reset();
        scheduler.unschedule(trigger);

        // test
        assertFalse(triggerHandler.wasTriggered());
        activateTrigger(trigger);
        assertFalse(triggerHandler.wasTriggered());
    }

    public void testTriggerStates() throws SchedulingException
    {
        Trigger trigger = createTrigger();
        assertEquals(TriggerState.NONE, trigger.getState());
        scheduler.schedule(trigger);
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
        scheduler.schedule(trigger);
        assertEquals(0, trigger.getTriggerCount());
        activateTrigger(trigger);
        assertEquals(1, trigger.getTriggerCount());
        scheduler.pause(trigger);
        activateTrigger(trigger);
        assertEquals(1, trigger.getTriggerCount());
        scheduler.resume(trigger);
        activateTrigger(trigger);
        assertEquals(2, trigger.getTriggerCount());
    }

    public void testTriggerCount() throws SchedulingException
    {
        // schedule
        Trigger trigger = createTrigger();
        scheduler.schedule(trigger);
        assertEquals(0, trigger.getTriggerCount());
        activateTrigger(trigger);
        assertEquals(1, trigger.getTriggerCount());
        activateTrigger(trigger);
        assertEquals(2, trigger.getTriggerCount());
        scheduler.unschedule(trigger);
        activateTrigger(trigger);
        assertEquals(2, trigger.getTriggerCount());
    }

    protected abstract Trigger createTrigger();
    protected abstract void activateTrigger(Trigger trigger) throws SchedulingException;
}