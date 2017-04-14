/* Copyright 2017 Zutubi Pty Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.zutubi.pulse.core.scm.api;

import java.io.File;
import java.util.concurrent.TimeUnit;

/**
 * A persistent context in which SCM operations executed on the master are run.
 * Note that multiple SCM operations may be run in parallel on different
 * threads.  Therefore SCM implementations that use this context (and the
 * persistent working directory that it exposes) must take care to use the
 * {@link #lock()} or {@link #tryLock(long, java.util.concurrent.TimeUnit)}
 * method to serialise access to the shared mutable data.
 * <p/>
 * A persistent context is not always available.  For example, some {@link ScmClient}
 * methods may be run without any defined project.  This will be documented in
 * the {@link ScmClient} interface.
 */
public interface PersistentContext
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
     * @return A persistent working directory available for use by the scm
     * implementation to persist data between invocations.
     */
    File getPersistentWorkingDir();
}
