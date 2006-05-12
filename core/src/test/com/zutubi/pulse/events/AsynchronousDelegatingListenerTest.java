/********************************************************************************
 @COPYRIGHT@
 ********************************************************************************/
package com.zutubi.pulse.events;

import com.zutubi.pulse.test.PulseTestCase;

/**
 * <class-comment/>
 */
public class AsynchronousDelegatingListenerTest extends PulseTestCase
{
    public AsynchronousDelegatingListenerTest()
    {
    }

    public AsynchronousDelegatingListenerTest(String name)
    {
        super(name);
    }

    protected void setUp() throws Exception
    {
        super.setUp();
    }

    protected void tearDown() throws Exception
    {
        super.tearDown();
    }

    public void testEventsExecutedOnSeparateThread() throws InterruptedException
    {
        CountHandleListener delegate = new CountHandleListener();
        AsynchronousDelegatingListener l = new AsynchronousDelegatingListener(delegate);

        l.handleEvent(new Event<Object>(this));
        l.handleEvent(new Event<Object>(this));
        l.handleEvent(new Event<Object>(this));
        // by asserting that the handled count is not equal to the number of
        // events that have been 'handled' we can verify that the events are
        // being handled on a separate thread.
        assertTrue(delegate.getHandledCount() != 3);
    }

    private class CountHandleListener implements EventListener
    {
        private int count;

        public void handleEvent(Event evt)
        {
            count++;
        }

        public int getHandledCount()
        {
            return count;
        }

        public Class[] getHandledEvents()
        {
            return new Class[]{Event.class};
        }
    }
}
