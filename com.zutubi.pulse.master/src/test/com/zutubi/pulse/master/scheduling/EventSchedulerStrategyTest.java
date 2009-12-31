package com.zutubi.pulse.master.scheduling;

import com.zutubi.events.DefaultEventManager;
import com.zutubi.events.Event;
import com.zutubi.events.EventManager;
import com.zutubi.util.bean.DefaultObjectFactory;

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
        ((EventSchedulerStrategy)scheduler).setObjectFactory(new DefaultObjectFactory());
        scheduler.setTriggerHandler(triggerHandler);
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

    public void testFiltersEvent() throws SchedulingException
    {
        EventTrigger trigger = new EventTrigger(TestEvent.class);
        trigger.setFilterClass(BlockFilter.class);
        scheduler.schedule(trigger);
        eventManager.publish(new TestEvent(this));
        assertFalse(triggerHandler.wasTriggered());
        assertEquals(0, trigger.getTriggerCount());
        trigger.setFilterClass(PassFilter.class);
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