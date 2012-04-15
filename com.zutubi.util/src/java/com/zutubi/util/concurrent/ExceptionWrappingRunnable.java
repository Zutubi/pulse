package com.zutubi.util.concurrent;

/**
 * An adapter to the runnable interface that can be used to run things that
 * throw checked exceptions.  These exceptions are rethrown as unchecked.
 */
public abstract class ExceptionWrappingRunnable implements Runnable
{
    public void run()
    {
        try
        {
            innerRun();
        }
        catch(Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    public abstract void innerRun() throws Exception;
}
