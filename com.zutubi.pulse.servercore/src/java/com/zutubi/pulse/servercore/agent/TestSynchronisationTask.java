package com.zutubi.pulse.servercore.agent;

import java.util.Properties;

/**
 * A synchronisation task that exists purely for testing purposes.
 */
public class TestSynchronisationTask extends SynchronisationTaskSupport implements SynchronisationTask
{
    private boolean succeed;

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

    public TestSynchronisationTask(Properties properties)
    {
        super(properties);
    }

    public Type getType()
    {
        return Type.TEST;
    }

    public void execute()
    {
        if (!succeed)
        {
            throw new RuntimeException("Test failure.");
        }
    }
}