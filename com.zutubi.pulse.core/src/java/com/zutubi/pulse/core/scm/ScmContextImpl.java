package com.zutubi.pulse.core.scm;

import com.zutubi.pulse.core.scm.api.ScmContext;
import com.zutubi.pulse.core.scm.api.ScmException;

import java.io.File;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

/**
 * A simple implementation of {@link com.zutubi.pulse.core.scm.api.ScmContext}.
 */
public class ScmContextImpl implements ScmContext
{
    private String projectName;
    private long projectHandle;
    private File persistentWorkingDir;
    private ReentrantLock lock = new ReentrantLock();

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
                throw new ScmException("Timed out waiting for exclusive access to ScmContext");
            }
        }
        catch (InterruptedException e)
        {
            throw new ScmException("Interrupted waiting for exclusive access to ScmContext");
        }
    }

    public void unlock()
    {
        lock.unlock();
    }

    public String getProjectName()
    {
        return projectName;
    }

    public void setProjectName(String projectName)
    {
        this.projectName = projectName;
    }

    public long getProjectHandle()
    {
        return projectHandle;
    }

    public void setProjectHandle(long projectHandle)
    {
        this.projectHandle = projectHandle;
    }

    public File getPersistentWorkingDir()
    {
        return persistentWorkingDir;
    }

    public void setPersistentWorkingDir(File dir)
    {
        this.persistentWorkingDir = dir;
    }
}
