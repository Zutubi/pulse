package com.zutubi.tove.transaction;

import com.zutubi.util.junit.ZutubiTestCase;
import junit.framework.AssertionFailedError;

public abstract class AbstractTransactionTestCase extends ZutubiTestCase
{
    protected void executeOnSeparateThreadAndWait(final Runnable r)
    {
        executeOnSeparateThreadAndWait(r, -1);
    }

    protected void executeOnSeparateThreadAndWait(final Runnable r, long timeout)
    {
        try
        {
            final AssertionFailedError[] afe = new AssertionFailedError[1];
            Thread thread = new Thread(new Runnable()
            {
                public void run()
                {
                    try
                    {
                        r.run();
                    }
                    catch (AssertionFailedError e)
                    {
                        afe[0] = e;
                    }
                }
            });
            thread.start();
            if (timeout == -1)
            {
                thread.join();
            }
            else
            {
                thread.join(timeout);
            }

            if (afe[0] != null)
            {
                throw afe[0];
            }
        }
        catch (InterruptedException e)
        {
            // noop.
        }
    }

    protected Thread executeOnSeparateThread(final Runnable r)
    {
        Thread thread = new Thread(new Runnable()
        {
            public void run()
            {
                r.run();
            }
        });
        thread.start();
        return thread;
    }

}
