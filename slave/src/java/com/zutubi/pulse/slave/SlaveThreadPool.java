package com.zutubi.pulse.slave;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 */
public class SlaveThreadPool
{
    private static ExecutorService pool;

    public SlaveThreadPool()
    {
        pool = Executors.newCachedThreadPool();
    }

    public void executeCommand(Runnable command)
    {
        pool.execute(command);
    }
}
