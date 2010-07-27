package com.zutubi.pulse.servercore.agent;

/**
 * An asynchronous synchronisation task that exists purely for testing purposes.
 */
public class TestAsyncSynchronisationTask implements SynchronisationTask
{
    private boolean succeed;

    public TestAsyncSynchronisationTask()
    {
    }

    /**
     * Create a new task for testing.
     *
     * @param succeed if true the task should succeed, if false it should throw
     *        an exception
     */
    public TestAsyncSynchronisationTask(boolean succeed)
    {
        this.succeed = succeed;
    }

    public boolean isSucceed()
    {
        return succeed;
    }

    public void execute()
    {
        if (!succeed)
        {
            throw new RuntimeException("Test failure.");
        }
    }
}
