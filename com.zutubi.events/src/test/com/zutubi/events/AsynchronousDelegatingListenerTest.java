package com.zutubi.events;

import com.zutubi.util.junit.ZutubiTestCase;

import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

public class AsynchronousDelegatingListenerTest extends ZutubiTestCase
{
    private Semaphore eventSemaphore = new Semaphore(0);
    private Semaphore doneSemaphore = new Semaphore(0);

    public void testEventsExecutedOnSeparateThread() throws InterruptedException
    {
        WaitListener delegate = new WaitListener();
        AsynchronousDelegatingListener l = new AsynchronousDelegatingListener(delegate, Executors.defaultThreadFactory());

        l.handleEvent(new Event(this));
        // the listener thread is now waiting for the semaphore to release.
        // we can only release it if it is indeed in a separate thread.
        eventSemaphore.release();

        assertTrue(doneSemaphore.tryAcquire(10, TimeUnit.SECONDS));
        assertTrue(delegate.acquired);
    }

    private class WaitListener implements EventListener
    {
        private boolean acquired;

        public void handleEvent(Event evt)
        {
            try
            {
                acquired = eventSemaphore.tryAcquire(10, TimeUnit.SECONDS);
            }
            catch (InterruptedException e)
            {
                e.printStackTrace();
            }

            doneSemaphore.release();
        }

        public Class[] getHandledEvents()
        {
            return new Class[]{Event.class};
        }
    }
}
