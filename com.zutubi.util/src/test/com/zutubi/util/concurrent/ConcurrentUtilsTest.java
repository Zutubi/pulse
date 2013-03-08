package com.zutubi.util.concurrent;

import com.zutubi.util.junit.ZutubiTestCase;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

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
}
