package com.cinnamonbob.event;

import junit.framework.TestCase;

import java.util.List;
import java.util.LinkedList;

/**
 * <class-comment/>
 */
public class DefaultEventManagerTest extends TestCase
{
    DefaultEventManager evtManager;

    RecordingEventListener listener;

    public DefaultEventManagerTest()
    {

    }

    public DefaultEventManagerTest(String testName)
    {
        super(testName);
    }

    public void setUp() throws Exception
    {
        super.setUp();
        
        evtManager = new DefaultEventManager(new SynchronousDispatcher());
        listener = new RecordingEventListener();
    }

    public void tearDown() throws Exception
    {
        // tear down here.
        
        super.tearDown();
    }

    public void testRegisterListener()
    {
        evtManager.register(listener);

        assertEquals(0, listener.getEventsReceived().size());
        evtManager.publish(new Event(this));
        assertEquals(1, listener.getEventsReceived().size());
    }

    public void testMultipleRegistrationsBySingleListener()
    {
        evtManager.register(listener);
        evtManager.register(listener);
        evtManager.publish(new Event(this));
        assertEquals(1, listener.getEventsReceived().size());
    }

    public void testUnregisterListener()
    {
        evtManager.register(listener);
        evtManager.unregister(listener);
        evtManager.publish(new Event(this));
        assertEquals(0, listener.getEventsReceived().size());
    }

    public void testListenerRegisteredInCallbackDoesNotReceiveEvent()
    {
        evtManager.register(new EventListener()
        {
            public void handleEvent(Event evt)
            {
                evtManager.register(listener);
            }
        });
        evtManager.publish(new Event(this));
        assertEquals(0, listener.getEventsReceived().size());
    }

    public void testListenerUnregisteredInCallbackStillReceivesEvent()
    {
        evtManager.register(new EventListener()
        {
            public void handleEvent(Event evt)
            {
                // need to ensure that the listener we are removing was not
                // triggered before it was removed, otherwise this test is meaningless.
                assertEquals(0, listener.getEventsReceived().size());
                evtManager.unregister(listener);
            }
        });
        evtManager.register(listener);
        evtManager.publish(new Event(this));
        assertEquals(1, listener.getEventsReceived().size());
    }

    private class RecordingEventListener implements EventListener
    {

        private final List<Event> events = new LinkedList<Event>();

        public void handleEvent(Event evt)
        {
            events.add(evt);
        }

        public List<Event> getEventsReceived()
        {
            return events;
        }
    }

    private void pause(long millis)
    {
        try
        {
            Thread.sleep(millis);
        }
        catch (InterruptedException e)
        {
            // noop
        }
    }
}
