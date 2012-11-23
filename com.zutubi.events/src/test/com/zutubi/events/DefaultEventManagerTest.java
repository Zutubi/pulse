package com.zutubi.events;

import junit.framework.TestCase;

import java.util.List;

public class DefaultEventManagerTest extends TestCase
{
    private DefaultEventManager eventManager;
    private RecordingEventListener listener;

    public void setUp() throws Exception
    {
        super.setUp();

        eventManager = new DefaultEventManager(new SynchronousDispatcher());
        listener = new RecordingEventListener(Event.class);
    }

    public void tearDown() throws Exception
    {
        super.tearDown();
    }

    public void testRegisterListener()
    {
        eventManager.register(listener);

        assertEquals(0, listener.getEventsReceived().size());
        eventManager.publish(new Event(this));
        assertEquals(1, listener.getEventsReceived().size());
    }

    public void testMultipleRegistrationsBySingleListener()
    {
        eventManager.register(listener);
        eventManager.register(listener);
        eventManager.publish(new Event(this));
        assertEquals(1, listener.getEventsReceived().size());
    }

    public void testUnregisterListener()
    {
        eventManager.register(listener);
        eventManager.unregister(listener);
        eventManager.publish(new Event(this));
        assertEquals(0, listener.getEventsReceived().size());
    }

    public void testListenerRegisteredInCallbackDoesNotReceiveEvent()
    {
        eventManager.register(new IgnoringEventListener(Event.class)
        {
            public void handleEvent(Event evt)
            {
                eventManager.register(listener);
            }
        });
        eventManager.publish(new Event(this));
        assertEquals(0, listener.getEventsReceived().size());
    }

    public void testListenerUnregisteredInCallbackStillReceivesEvent()
    {
        // two listeners, both unregister the other one, and both are checked to
        // ensure that they receive the published events.

        final RecordingEventListener[] listeners = new RecordingEventListener[2];

        listeners[0] = new RecordingEventListener(Event.class)
        {
            public void handleEvent(Event evt)
            {
                super.handleEvent(evt);
                eventManager.unregister(listeners[1]);
            }
        };
        listeners[1] = new RecordingEventListener(Event.class)
        {
            public void handleEvent(Event evt)
            {
                super.handleEvent(evt);
                eventManager.unregister(listeners[0]);
            }
        };
        eventManager.register(listeners[0]);
        eventManager.register(listeners[1]);
        eventManager.publish(new Event(this));
        assertEquals(1, listeners[0].getEventsReceived().size());
        assertEquals(1, listeners[1].getEventsReceived().size());
    }

    public void testHandleByClassPartA()
    {
        RecordingEventListener listener = new RecordingEventListener(TestEvent.class);

        eventManager.register(listener);
        eventManager.publish(new Event(this));
        assertEquals(0, listener.getReceivedCount());
        eventManager.publish(new TestEvent(this));
        assertEquals(1, listener.getReceivedCount());

        listener.reset();
        eventManager.unregister(listener);
        eventManager.publish(new Event(this));
        eventManager.publish(new TestEvent(this));
        assertEquals(0, listener.getReceivedCount());
    }

    public void testHandleByClassPartB()
    {
        RecordingEventListener listener = new RecordingEventListener(Event.class);

        eventManager.register(listener);
        eventManager.publish(new Event(this));
        assertEquals(1, listener.getReceivedCount());
        eventManager.publish(new TestEvent(this));
        assertEquals(2, listener.getReceivedCount());

        listener.reset();
        eventManager.unregister(listener);
        eventManager.publish(new Event(this));
        eventManager.publish(new TestEvent(this));
        assertEquals(0, listener.getReceivedCount());
    }

    public void testReceiveAllEventsByDefault()
    {
        RecordingEventListener listener = new RecordingEventListener();

        eventManager.register(listener);
        eventManager.publish(new Event(this));
        assertEquals(1, listener.getReceivedCount());
        eventManager.publish(new TestEvent(this));
        assertEquals(2, listener.getReceivedCount());
    }

    public void testHandleByInterfacePartA()
    {
        RecordingEventListener listener = new RecordingEventListener(TestInterface.class);

        eventManager.register(listener);
        eventManager.publish(new TestEvent(this));
        assertEquals(1, listener.getReceivedCount());
        eventManager.publish(new Event(this));
        assertEquals(1, listener.getReceivedCount());
    }

    public void testHandleByInterfacePartB()
    {
        RecordingEventListener listener = new RecordingEventListener(BaseInterface.class);

        eventManager.register(listener);
        eventManager.publish(new TestEvent(this));
        assertEquals(1, listener.getReceivedCount());
        eventManager.publish(new Event(this));
        assertEquals(1, listener.getReceivedCount());
    }

    public void testPublishNull()
    {
        eventManager.register(listener);
        eventManager.publish(null);
        assertEquals(0, listener.getReceivedCount());
    }

    public void testDefaultDispatcherIsSynchronous()
    {
        EventManager defaultConstructedManager = new DefaultEventManager();
        final Thread thread = Thread.currentThread();
        final boolean[] sameThread = new boolean[]{false};
        defaultConstructedManager.register(new EventListener()
        {
            public void handleEvent(Event event)
            {
                sameThread[0] = thread == Thread.currentThread();
            }

            public Class[] getHandledEvents()
            {
                return null;
            }
        });
        
        defaultConstructedManager.publish(new Event(this));
        assertTrue("handleEvent() called by a different thread to that used to call publish()", sameThread[0]);
    }

    public void testImmediatePublishEventPending()
    {
        final OuterEvent outerEvent = new OuterEvent(this);
        final InnerEvent innerEvent = new InnerEvent(this);

        final RecordingEventListener listener = new RecordingEventListener(Event.class);
        RecordingEventListener publishingListener = new RecordingEventListener(OuterEvent.class)
        {
            public void handleEvent(Event event)
            {
                eventManager.publish(innerEvent);

                // We don't know if the listener has got the outer event yet,
                // but it must have received the inner one when publish
                // returns.
                assertTrue(listener.getEventsReceived().contains(innerEvent));
            }
        };

        eventManager.register(listener);
        eventManager.register(publishingListener);
        eventManager.publish(outerEvent);
    }

    public void testDeferredPublishNonePending()
    {
        RecordingEventListener listener = new RecordingEventListener(Event.class);
        eventManager.register(listener);
        Event event = new Event(this);
        eventManager.publish(event, PublishFlag.DEFERRED);
        assertReceived(listener, event);
    }

    public void testDeferredPublishAfterPublishNonePending()
    {
        RecordingEventListener listener = new RecordingEventListener(Event.class);
        eventManager.register(listener);
        Event e1 = new Event(this);
        Event e2 = new Event(this);
        eventManager.publish(e1);
        eventManager.publish(e2, PublishFlag.DEFERRED);
        assertReceived(listener, e1, e2);
    }

    public void testDeferredPublishEventPending()
    {
        final OuterEvent outerEvent = new OuterEvent(this);
        final InnerEvent innerEvent = new InnerEvent(this);

        final RecordingEventListener listener = new RecordingEventListener(Event.class);
        RecordingEventListener publishingListener = new RecordingEventListener(OuterEvent.class)
        {
            public void handleEvent(Event event)
            {
                eventManager.publish(innerEvent, PublishFlag.DEFERRED);
                // The deferred event should not be published to the other
                // listener yet.
                assertFalse(listener.getEventsReceived().contains(innerEvent));
            }
        };

        eventManager.register(listener);
        eventManager.register(publishingListener);

        eventManager.publish(outerEvent);

        // Now the outer publish is done, the deferred event should have been
        // delivered.
        assertReceived(listener, outerEvent, innerEvent);
    }

    public void testMultipleDeferredPublishesEventPending()
    {
        final OuterEvent outerEvent = new OuterEvent(this);
        final InnerEvent innerEvent1 = new InnerEvent(this);
        final InnerEvent innerEvent2 = new InnerEvent(this);

        final RecordingEventListener listener = new RecordingEventListener(Event.class);
        RecordingEventListener publishingListener = new RecordingEventListener(OuterEvent.class)
        {
            public void handleEvent(Event event)
            {
                eventManager.publish(innerEvent1, PublishFlag.DEFERRED);
                eventManager.publish(innerEvent2, PublishFlag.DEFERRED);
                assertFalse(listener.getEventsReceived().contains(innerEvent1));
                assertFalse(listener.getEventsReceived().contains(innerEvent2));
            }
        };

        eventManager.register(listener);
        eventManager.register(publishingListener);

        eventManager.publish(outerEvent);

        // Verify that the inner events both come out, and in the right
        // order.
        assertReceived(listener, outerEvent, innerEvent1, innerEvent2);
    }

    public void testDeferredPublishNestedEventsPending()
    {
        final OuterEvent outerEvent = new OuterEvent(this);
        final MiddleEvent middleEvent = new MiddleEvent(this);
        final InnerEvent innerEvent = new InnerEvent(this);

        final RecordingEventListener listener = new RecordingEventListener(Event.class);
        RecordingEventListener outerListener = new RecordingEventListener(OuterEvent.class)
        {
            public void handleEvent(Event event)
            {
                eventManager.publish(middleEvent);
                assertTrue(listener.getEventsReceived().contains(middleEvent));
                // The deferred event should now be delivered, as it is only
                // deferred one level.
                assertTrue(listener.getEventsReceived().contains(innerEvent));
            }
        };

        RecordingEventListener middleListener = new RecordingEventListener(MiddleEvent.class)
        {
            public void handleEvent(Event event)
            {
                eventManager.publish(innerEvent, PublishFlag.DEFERRED);
                assertFalse(listener.getEventsReceived().contains(innerEvent));
            }
        };

        eventManager.register(listener);
        eventManager.register(middleListener);
        eventManager.register(outerListener);

        eventManager.publish(outerEvent);

        // Now the outer publish is done, all events should have been
        // delivered.  The inner one must come after the middle one, but we
        // can't be sure where the outer one falls.
        assertEquals(3, listener.getReceivedCount());
        List<Event> received = listener.getEventsReceived();
        assertTrue(received.remove(outerEvent));
        assertEvents(received, middleEvent, innerEvent);
    }

    public void testDeferredPublishAtTwoNestingLevels()
    {
        final OuterEvent outerEvent = new OuterEvent(this);
        final MiddleEvent middleEvent = new MiddleEvent(this);
        final InnerEvent innerEvent = new InnerEvent(this);

        final RecordingEventListener listener = new RecordingEventListener(Event.class);
        RecordingEventListener outerListener = new RecordingEventListener(OuterEvent.class)
        {
            public void handleEvent(Event event)
            {
                eventManager.publish(middleEvent, PublishFlag.DEFERRED);
                assertFalse(listener.getEventsReceived().contains(middleEvent));
                assertFalse(listener.getEventsReceived().contains(innerEvent));
            }
        };

        RecordingEventListener middleListener = new RecordingEventListener(MiddleEvent.class)
        {
            public void handleEvent(Event event)
            {
                eventManager.publish(innerEvent, PublishFlag.DEFERRED);
                assertFalse(listener.getEventsReceived().contains(innerEvent));
            }
        };

        eventManager.register(listener);
        eventManager.register(middleListener);
        eventManager.register(outerListener);

        eventManager.publish(outerEvent);

        // In this case we know the full order, as both cascaded events are deferred.
        assertReceived(listener, outerEvent, middleEvent, innerEvent);
    }

    private void assertReceived(RecordingEventListener listener, Event... expectedEvents)
    {
        assertEvents(listener.getEventsReceived(), expectedEvents);
    }

    private void assertEvents(List<Event> gotEvents, Event... expectedEvents)
    {
        assertEquals(expectedEvents.length, gotEvents.size());
        for (int i = 0; i < expectedEvents.length; i++)
        {
            assertSame(expectedEvents[i], gotEvents.get(i));
        }
    }

    private class IgnoringEventListener implements EventListener
    {
        private final Class[] handledEvents;

        public IgnoringEventListener(Class... handledEvents)
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

    private static class TestEvent extends Event implements TestInterface
    {
        public TestEvent(Object source)
        {
            super(source);
        }
    }

    private static class OuterEvent extends Event
    {
        public OuterEvent(Object source)
        {
            super(source);
        }
    }

    private static class MiddleEvent extends Event
    {
        public MiddleEvent(Object source)
        {
            super(source);
        }
    }

    private static class InnerEvent extends Event
    {
        public InnerEvent(Object source)
        {
            super(source);
        }
    }

    private static interface BaseInterface
    {
    }

    private static interface TestInterface extends BaseInterface
    {
    }
}
