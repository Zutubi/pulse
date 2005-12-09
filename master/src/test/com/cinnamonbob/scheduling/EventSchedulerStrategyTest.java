package com.cinnamonbob.scheduling;

import com.cinnamonbob.core.event.DefaultEventManager;
import com.cinnamonbob.core.event.Event;
import com.cinnamonbob.core.event.EventManager;

/**
 * <class-comment/>
 */
public class EventSchedulerStrategyTest extends BaseSchedulerStrategyTest
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
        NoopTask task = new NoopTask("testName", "testGroup");
        scheduler.schedule(trigger, task);

        assertFalse(triggerHandler.wasTriggered());
        eventManager.publish(new Event(this));
        assertFalse(triggerHandler.wasTriggered());
        assertEquals(0, trigger.getTriggerCount());
        eventManager.publish(new TestEvent(this));
        assertTrue(triggerHandler.wasTriggered());
        assertEquals(1, trigger.getTriggerCount());
    }

    protected void activateTrigger(Trigger trigger, Task task)
    {
        eventManager.publish(new Event(this));
    }

    protected Trigger createTrigger()
    {
        return new EventTrigger();
    }

    private class TestEvent extends Event
    {
        public TestEvent(Object source)
        {
            super(source);
        }
    }
}