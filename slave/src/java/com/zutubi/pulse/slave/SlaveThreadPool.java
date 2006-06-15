package com.zutubi.pulse.slave;

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 */
public class SlaveThreadPool implements Executor
{
    private static ExecutorService pool;

    public SlaveThreadPool()
    {
        pool = Executors.newCachedThreadPool();
    }

    public void execute(Runnable command)
    {
        pool.execute(command);
    }
}
