package com.zutubi.util;

import com.zutubi.util.junit.ZutubiTestCase;

import java.util.concurrent.*;
import java.util.List;
import java.util.LinkedList;

public class ConcurrentUtilsTest extends ZutubiTestCase
{
    private static final Integer TASK_RESULT    = 0;
    private static final Integer DEFAULT_RESULT = 1;

    public void testRunWithTimeoutTaskCompletes()
    {
        Integer result = ConcurrentUtils.runWithTimeout(new Callable<Integer>()
        {
            public Integer call() throws Exception
            {
                return TASK_RESULT;
            }
        }, 10, TimeUnit.SECONDS, DEFAULT_RESULT);

        assertEquals(TASK_RESULT, result);
    }

    public void testRunWithTimeoutTaskHangs()
    {
        Integer result = ConcurrentUtils.runWithTimeout(new Callable<Integer>()
        {
            public Integer call() throws Exception
            {
                Thread.sleep(1000);
                return TASK_RESULT;
            }
        }, 1, TimeUnit.MILLISECONDS, DEFAULT_RESULT);

        assertEquals(DEFAULT_RESULT, result);
    }

    public void testRunWithTimeoutTaskThrows()
    {
        try
        {
            ConcurrentUtils.runWithTimeout(new Callable<Integer>()
            {
                public Integer call() throws Exception
                {
                    throw new Exception("ouch");
                }
            }, 10, TimeUnit.SECONDS, DEFAULT_RESULT);

            fail("Task-thrown exception should have been propagated as a RuntimeException");
        }
        catch (RuntimeException e)
        {
            assertTrue(e.getMessage().contains("ouch"));
        }
    }

    public void testWaitForTasks() throws InterruptedException
    {
        ExecutorService executor = Executors.newFixedThreadPool(2);

        // create some processes that do stuff.
        List<SleepyProcess> processes = createProcesses(10);
        List<Future> futures = new LinkedList<Future>();
        for (Runnable process : processes)
        {
            futures.add(executor.submit(process));
        }
        // wait for them to complete before continuing.
        ConcurrentUtils.waitForTasks(futures, null);

        // assert that they are completed as expected.
        for (SleepyProcess process : processes)
        {
            assertTrue(process.isCompleted());
        }
    }

    private List<SleepyProcess> createProcesses(int count)
    {
        List<SleepyProcess> processes = new LinkedList<SleepyProcess>();
        for (int i = 0; i < count; i++)
        {
            processes.add(new SleepyProcess());
        }
        return processes;
    }

    private class SleepyProcess implements Runnable
    {
        private boolean completed;

        public void run()
        {
            try
            {
                Thread.sleep(100);
                completed = true;
            }
            catch (InterruptedException e)
            {
                // noop.
            }
        }

        public boolean isCompleted()
        {
            return completed;
        }
    }
}
