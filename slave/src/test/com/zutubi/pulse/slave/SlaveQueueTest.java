package com.zutubi.pulse.slave;

import com.zutubi.pulse.test.PulseTestCase;

import java.util.concurrent.Executor;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

/**
 */
public class SlaveQueueTest extends PulseTestCase
{
    private boolean wait = false;
    private SlaveQueue queue;
    private MockExecutor executor;
    private Semaphore startSemaphore;
    private Semaphore doneSemaphore;

    protected void setUp() throws Exception
    {
        queue = new SlaveQueue();
        executor = new MockExecutor();
        queue.setExecutor(executor);
        startSemaphore = new Semaphore(0);
        doneSemaphore = new Semaphore(0);

        super.setUp();
    }

    protected void tearDown() throws Exception
    {
        super.tearDown();

        queue = null;
        executor = null;
        startSemaphore = null;
        doneSemaphore = null;
    }

    public void testStartStop() throws InterruptedException
    {
        queue.enqueue(new MockRunnable());
        assertEquals(1, queue.size());
        queue.start();
        assertTrue(doneSemaphore.tryAcquire(30, TimeUnit.SECONDS));
        assertEquals(0, queue.size());

        queue.stop();
        queue.enqueue(new MockRunnable());
        assertEquals(1, queue.size());
        assertFalse(doneSemaphore.tryAcquire(2, TimeUnit.SECONDS));
        assertEquals(1, queue.size());

        queue.start();
        assertTrue(doneSemaphore.tryAcquire(30, TimeUnit.SECONDS));
        assertEquals(0, queue.size());
    }

    public void testExclusiveOK()
    {
        assertTrue(queue.enqueueExclusive(new MockRunnable()));
    }
    
    public void testExclusiveFail()
    {
        assertTrue(queue.enqueueExclusive(new MockRunnable()));
        assertFalse(queue.enqueueExclusive(new MockRunnable()));
    }

    private class MockRunnable implements Runnable
    {
        public void run()
        {
        }
    }

    private class MockExecutor implements Executor
    {
        public void execute(Runnable command)
        {
            try
            {
                if(wait)
                {
                    startSemaphore.acquire();
                }
                doneSemaphore.release();
            }
            catch (InterruptedException e)
            {
                e.printStackTrace();
            }
        }
    }
}
