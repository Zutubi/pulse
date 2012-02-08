package com.zutubi.pulse.core.scm;

import com.zutubi.pulse.core.scm.api.PersistentContext;
import com.zutubi.pulse.core.scm.api.ScmException;

import java.io.File;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

/**
 * A simple implementation of {@link PersistentContext}.
 */
public class PersistentContextImpl implements PersistentContext
{
    private File          persistentWorkingDir;
    private ReentrantLock lock = new ReentrantLock();

    public PersistentContextImpl(File persistentWorkingDir)
    {
        this.persistentWorkingDir = persistentWorkingDir;
    }

    public void lock()
    {
        lock.lock();
    }

    public void tryLock(long timeout, TimeUnit timeUnit) throws ScmException
    {
        try
        {
            if (!lock.tryLock(timeout, timeUnit))
            {
                throw new ScmException("Timed out waiting for exclusive access to persistent context");
            }
        }
        catch (InterruptedException e)
        {
            throw new ScmException("Interrupted waiting for exclusive access to persistent context");
        }
    }

    public void unlock()
    {
        lock.unlock();
    }

    public File getPersistentWorkingDir()
    {
        return persistentWorkingDir;
    }
}
