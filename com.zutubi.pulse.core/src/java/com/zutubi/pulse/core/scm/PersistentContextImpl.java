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
