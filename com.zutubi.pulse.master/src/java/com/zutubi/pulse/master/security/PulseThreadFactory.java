package com.zutubi.pulse.master.security;

import java.util.concurrent.ThreadFactory;

/**
 * A factory used to create system threads that have the privilege to do as
 * they please.
 */
public class PulseThreadFactory implements ThreadFactory
{
    public Thread newThread(Runnable r)
    {
        return new Thread(new DelegatingRunnable(r));
    }

    public Thread newThread(Runnable r, String name)
    {
        return new Thread(new DelegatingRunnable(r), name);
    }

    private class DelegatingRunnable implements Runnable
    {
        private Runnable delegate;

        public DelegatingRunnable(Runnable delegate)
        {
            this.delegate = delegate;
        }

        public void run()
        {
            SecurityUtils.loginAsSystem();
            delegate.run();
        }
    }
}
