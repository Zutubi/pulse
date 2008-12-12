package com.zutubi.pulse.core.scm.api;

import java.io.File;
import java.util.concurrent.TimeUnit;

/**
 * The context in which SCM operations executed outside of the build are run.
 * Note that multiple SCM operations may be run in parallel on different
 * threads.  All operations that use a context must therefore acquire exclusive
 * access using {@link #lock()} or {@link #tryLock(long, java.util.concurrent.TimeUnit)}
 * to ensure safety.
 */
public interface ScmContext
{
    /**
     * Acquire exclusive access to the context.  Should be held for the
     * shortest period possible, and released using {@link #unlock()} when
     * done.
     *
     * @see #unlock()
     * @see #tryLock(long, java.util.concurrent.TimeUnit)
     */
    void lock();

    /**
     * Tries to acquire exclusive access to the context for up to the given
     * timeout.   If the lock cannot be acquired in time an exception is
     * thrown.  The lock should be held for the shortest period possible, and
     * released using {@link #unlock()} when done.
     *
     * @param timeout timeout period to wait (in the given units)
     * @param units   units for the timeout period
     * @throws ScmException if the lock could not be acquired within the
     *                      timeout
     *
     * @see #unlock()
     * @see #lock()
     */
    void tryLock(long timeout, TimeUnit units) throws ScmException;

    /**
     * Release access to this context.
     *
     * @see #lock()
     * @see #tryLock(long, java.util.concurrent.TimeUnit) 
     */
    void unlock();

    /**
     * Name of the project to which this context applies.
     *
     * @return the project name
     */
    String getProjectName();

    /**
     * Handle of the project to which this context applies.
     *
     * @return the project handle (a unique, permanant id for the project)
     */
    long getProjectHandle();

    /**
     * @return a persistent working directory available for use by the scm
     * implementation to persist data between invocations.
     */
    File getPersistentWorkingDir();
}
