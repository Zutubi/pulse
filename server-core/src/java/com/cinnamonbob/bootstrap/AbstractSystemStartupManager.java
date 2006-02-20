package com.cinnamonbob.bootstrap;

/**
 * <class-comment/>
 */
public abstract class AbstractSystemStartupManager implements SystemStartupManager
{
    protected StartupCallback callback;

    public static long NOT_STARTED = -1;
    private long startTime = NOT_STARTED;
    private boolean started = false;

    public void addCallback(StartupCallback callback)
    {
        this.callback = callback;
    }

    public long getStartTime()
    {
        return startTime;
    }

    public long getUptime()
    {
        if (startTime != NOT_STARTED)
        {
            return System.currentTimeMillis() - startTime;
        }
        return NOT_STARTED;
    }

    public boolean isSystemStarted()
    {
        return started;
    }

    private void setStarted(boolean b)
    {
        started = b;
        if (started)
        {
            startTime = System.currentTimeMillis();
            callback.done();
        }
    }

    public void startup() throws StartupException
    {
        runStartup();
        setStarted(true);
    }

    protected abstract void runStartup() throws StartupException;
}
