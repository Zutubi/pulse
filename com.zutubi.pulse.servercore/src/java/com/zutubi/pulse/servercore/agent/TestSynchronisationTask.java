package com.zutubi.pulse.servercore.agent;

/**
 * A synchronisation task that exists purely for testing purposes.
 */
public class TestSynchronisationTask implements SynchronisationTask
{
    private boolean succeed;

    public TestSynchronisationTask()
    {
    }

    /**
     * Create a new task for testing.
     *
     * @param succeed if true the task should succeed, if false it should throw
     *        an exception
     */
    public TestSynchronisationTask(boolean succeed)
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