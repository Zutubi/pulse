package com.zutubi.pulse.slave;

import java.util.concurrent.ThreadFactory;

/**
 * Simple thread factory for the slave.
 */
public class SlaveThreadFactory implements ThreadFactory
{
    public Thread newThread(Runnable runnable)
    {
        return new Thread(runnable);
    }
}
