package com.zutubi.pulse.events;

import com.zutubi.events.AllEventListener;
import com.zutubi.events.DefaultEventManager;
import com.zutubi.events.Event;
import com.zutubi.events.EventManager;
import com.zutubi.pulse.test.PulseTestCase;

/**
 *
 *
 */
public class AllEventListenerTest extends PulseTestCase
{
    private EventManager eventManager = null;

    protected void setUp() throws Exception
    {
        super.setUp();
        
        eventManager = new DefaultEventManager();
    }

    protected void tearDown() throws Exception
    {
        super.tearDown();
    }

    public void testReceiveBaseEvent()
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
        
        eventManager.publish(new Event(this));
        assertTrue(handled[0]);
    }

    public void testReceieveAnnonymousEvent()
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

        eventManager.publish(new Event(this)
        {
            // noop extension to make this an annonymous inner.
        });
        assertTrue(handled[0]);
    }
}
