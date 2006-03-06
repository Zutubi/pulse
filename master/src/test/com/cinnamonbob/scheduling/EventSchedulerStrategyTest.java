package com.cinnamonbob.scheduling;

import com.cinnamonbob.events.DefaultEventManager;
import com.cinnamonbob.events.Event;
import com.cinnamonbob.events.EventManager;

/**
 * <class-comment/>
 */
public class EventSchedulerStrategyTest extends SchedulerStrategyTestBase
{
    private EventManager eventManager;

    public EventSchedulerStrategyTest(String testName)
    {
        super(testName);
    }

    public void setUp() throws Exception
    {
        super.setUp();

        // add setup code here.
        scheduler = new EventSchedulerStrategy();
        eventManager = new DefaultEventManager();
        ((EventSchedulerStrategy)scheduler).setEventManager(eventManager);
        scheduler.setTriggerHandler(triggerHandler);
    }

    public void tearDown() throws Exception
    {
        // add tear down code here.
        eventManager = null;
        scheduler = null;

        super.tearDown();
    }

    public void testTriggerOnSpecificEvent() throws SchedulingException
    {
        EventTrigger trigger = new EventTrigger(TestEvent.class);
        scheduler.schedule(trigger);

        assertFalse(triggerHandler.wasTriggered());
        eventManager.publish(new Event(this));
        assertFalse(triggerHandler.wasTriggered());
        assertEquals(0, trigger.getTriggerCount());
        eventManager.publish(new TestEvent(this));
        assertTrue(triggerHandler.wasTriggered());
        assertEquals(1, trigger.getTriggerCount());
    }

    protected void activateTrigger(Trigger trigger)
    {
        eventManager.publish(new Event(this));
    }

    protected Trigger createTrigger()
    {
        Trigger trigger = new EventTrigger();
        trigger.setTaskClass(NoopTask.class);
        return trigger;
    }

    private class TestEvent extends Event
    {
        public TestEvent(Object source)
        {
            super(source);
        }
    }
}