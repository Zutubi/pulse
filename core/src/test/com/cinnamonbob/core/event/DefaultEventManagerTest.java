package com.cinnamonbob.core.event;

import junit.framework.TestCase;

import java.util.LinkedList;
import java.util.List;

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
        listener = new RecordingEventListener(new Class[]{Event.class});
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
        evtManager.register(new MockEventListener(new Class[]{Event.class})
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
        // two listeners, both unregister the other one, and both are checked to
        // ensure that they receive the published events.

        final RecordingEventListener[] listeners = new RecordingEventListener[2];

        listeners[0] = new RecordingEventListener(new Class[]{Event.class})
        {
            public void handleEvent(Event evt)
            {
                super.handleEvent(evt);
                evtManager.unregister(listeners[1]);
            }
        };
        listeners[1] = new RecordingEventListener(new Class[]{Event.class})
        {
            public void handleEvent(Event evt)
            {
                super.handleEvent(evt);
                evtManager.unregister(listeners[0]);
            }
        };
        evtManager.register(listeners[0]);
        evtManager.register(listeners[1]);
        evtManager.publish(new Event(this));
        assertEquals(1, listeners[0].getEventsReceived().size());
        assertEquals(1, listeners[1].getEventsReceived().size());
    }

    public void testHandleByClassPartA()
    {
        RecordingEventListener listener = new RecordingEventListener(new Class[]{TestEvent.class});

        evtManager.register(listener);
        evtManager.publish(new Event(this));
        assertEquals(0, listener.getReceivedCount());
        evtManager.publish(new TestEvent(this));
        assertEquals(1, listener.getReceivedCount());

        listener.reset();
        evtManager.unregister(listener);
        evtManager.publish(new Event(this));
        evtManager.publish(new TestEvent(this));
        assertEquals(0, listener.getReceivedCount());
    }

    public void testHandleByClassPartB()
    {
        RecordingEventListener listener = new RecordingEventListener(new Class[]{Event.class});

        evtManager.register(listener);
        evtManager.publish(new Event(this));
        assertEquals(1, listener.getReceivedCount());
        evtManager.publish(new TestEvent(this));
        assertEquals(2, listener.getReceivedCount());

        listener.reset();
        evtManager.unregister(listener);
        evtManager.publish(new Event(this));
        evtManager.publish(new TestEvent(this));
        assertEquals(0, listener.getReceivedCount());
    }

    public void testReceiveAllEventsByDefault()
    {
        RecordingEventListener listener = new RecordingEventListener(new Class[]{});

        evtManager.register(listener);
        evtManager.publish(new Event(this));
        assertEquals(1, listener.getReceivedCount());
        evtManager.publish(new TestEvent(this));
        assertEquals(2, listener.getReceivedCount());
    }

    public void testHandleByInterfacePartA()
    {
        RecordingEventListener listener = new RecordingEventListener(new Class[]{TestInterface.class});

        evtManager.register(listener);
        evtManager.publish(new TestEvent(this));
        assertEquals(1, listener.getReceivedCount());
        evtManager.publish(new Event(this));
        assertEquals(1, listener.getReceivedCount());
    }

    public void testHandleByInterfacePartB()
    {
        RecordingEventListener listener = new RecordingEventListener(new Class[]{BaseInterface.class});

        evtManager.register(listener);
        evtManager.publish(new TestEvent(this));
        assertEquals(1, listener.getReceivedCount());
        evtManager.publish(new Event(this));
        assertEquals(1, listener.getReceivedCount());
    }

    private class MockEventListener implements EventListener
    {
        private final Class[] handledEvents;

        public MockEventListener(Class[] handledEvents)
        {
            this.handledEvents = handledEvents;
        }

        public Class[] getHandledEvents()
        {
            return handledEvents;
        }

        public void handleEvent(Event evt)
        {

        }
    }

    private class RecordingEventListener extends MockEventListener
    {

        private final List<Event> events = new LinkedList<Event>();

        public RecordingEventListener(Class[] handledEvents)
        {
            super(handledEvents);
        }

        public void handleEvent(Event evt)
        {
            events.add(evt);
        }

        public List<Event> getEventsReceived()
        {
            return events;
        }

        public int getReceivedCount()
        {
            return getEventsReceived().size();
        }

        public void reset()
        {
            getEventsReceived().clear();
        }
    }

    private class TestEvent extends Event implements TestInterface
    {
        public TestEvent(Object source)
        {
            super(source);
        }
    }

    private interface BaseInterface
    {

    }

    private interface TestInterface extends BaseInterface
    {

    }
}
