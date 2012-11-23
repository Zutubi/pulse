package com.zutubi.events;

import junit.framework.TestCase;

public class AllEventListenerTest extends TestCase
{
    private EventManager eventManager = null;

    protected void setUp() throws Exception
    {
        super.setUp();
        
        eventManager = new DefaultEventManager();
    }

    public void testReceiveBaseEvent()
    {
        eventReceivedTest(new Event(this));
    }

    public void testReceiveSubEvent()
    {
        eventReceivedTest(new SubEvent(this));
    }

    public void testReceiveAnonymousEvent()
    {
        eventReceivedTest(new Event(this)
        {
            // noop extension
        });
    }

    private void eventReceivedTest(Event e)
    {
        final boolean[] handled = {false};
        eventManager.register(new AllEventListener()
        {
            public void handleEvent(Event event)
            {
                handled[0] = true;
            }
        });

        assertFalse(handled[0]);
        eventManager.publish(e);
        assertTrue(handled[0]);
    }

    private static class SubEvent extends Event
    {
        public SubEvent(Object source)
        {
            super(source);
        }
    }
}
