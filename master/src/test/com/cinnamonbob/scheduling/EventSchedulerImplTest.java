package com.cinnamonbob.scheduling;

import com.cinnamonbob.core.event.DefaultEventManager;
import com.cinnamonbob.core.event.Event;
import com.cinnamonbob.core.event.EventManager;

/**
 * <class-comment/>
 */
public class EventSchedulerImplTest extends SchedulerImplTest
{
    private EventManager eventManager;

    public EventSchedulerImplTest(String testName)
    {
        super(testName);
    }

    public void setUp() throws Exception
    {
        super.setUp();

        // add setup code here.
        scheduler = new EventSchedulerImpl();
        eventManager = new DefaultEventManager();
        ((EventSchedulerImpl)scheduler).setEventManager(eventManager);
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
        TestTask task = new TestTask("testName", "testGroup");
        scheduler.schedule(trigger, task);
        assertFalse(task.isExecuted());
        eventManager.publish(new Event(this));
        assertFalse(task.isExecuted());
        assertEquals(0, trigger.getTriggerCount());
        eventManager.publish(new TestEvent(this));
        assertTrue(task.isExecuted());
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