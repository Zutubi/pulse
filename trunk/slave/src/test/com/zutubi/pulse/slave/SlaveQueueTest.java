package com.zutubi.pulse.slave;

import com.zutubi.pulse.test.PulseTestCase;

import java.util.concurrent.Executor;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 */
public class SlaveQueueTest extends PulseTestCase
{
    private boolean wait = false;
    private SlaveQueue queue;
    private Semaphore startSemaphore;
    private Semaphore doneSemaphore;
    private int runCount = 0;
    private Lock runCountLock = new ReentrantLock();

    protected void setUp() throws Exception
    {
        queue = new SlaveQueue();
        startSemaphore = new Semaphore(0);
        doneSemaphore = new Semaphore(0);

        super.setUp();
    }

    protected void tearDown() throws Exception
    {
        super.tearDown();

        queue = null;
        startSemaphore = null;
        doneSemaphore = null;
    }

    public void testStartStop() throws InterruptedException
    {
        useMockExecutor();

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
        useMockExecutor();

        assertTrue(queue.enqueueExclusive(new MockRunnable()));
    }

    public void testExclusiveFail()
    {
        useMockExecutor();

        assertTrue(queue.enqueueExclusive(new MockRunnable()));
        assertFalse(queue.enqueueExclusive(new MockRunnable()));
    }

    public void testSerialised() throws InterruptedException
    {
        queue.enqueue(new MockRunnable(10000));
        queue.enqueue(new MockRunnable());
        queue.start();

        Thread.sleep(100);
        runCountLock.lock();
        try
        {
            assertTrue(runCount < 2);
        }
        finally
        {
            runCountLock.unlock();
        }
    }

    private void useMockExecutor()
    {
        queue.setExecutor(new MockExecutor());
    }

    private class MockRunnable implements Runnable
    {
        long sleepTime = 0;

        public MockRunnable()
        {
        }

        public MockRunnable(long sleepTime)
        {
            this.sleepTime = sleepTime;
        }

        public void run()
        {
            runCountLock.lock();
            runCount++;
            runCountLock.unlock();

            if(sleepTime > 0)
            {
                try
                {
                    Thread.sleep(sleepTime);
                }
                catch (InterruptedException e)
                {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }
            }
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
