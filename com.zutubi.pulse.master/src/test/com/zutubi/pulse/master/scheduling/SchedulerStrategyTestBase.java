package com.zutubi.pulse.master.scheduling;

import com.zutubi.util.junit.ZutubiTestCase;

public abstract class SchedulerStrategyTestBase extends ZutubiTestCase
{
    protected SchedulerStrategy scheduler = null;
    protected TestTriggerHandler triggerHandler = null;

    public SchedulerStrategyTestBase(String testName)
    {
        super(testName);
    }

    public void setUp() throws Exception
    {
        super.setUp();
        triggerHandler = new TestTriggerHandler();
    }

    public void testTaskExecutedOnTrigger() throws SchedulingException
    {
        Trigger trigger = createTrigger();
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