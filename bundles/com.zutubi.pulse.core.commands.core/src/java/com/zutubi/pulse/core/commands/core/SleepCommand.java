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

package com.zutubi.pulse.core.commands.core;

import com.zutubi.pulse.core.commands.api.CommandContext;
import com.zutubi.pulse.core.commands.api.CommandSupport;

import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

/**
 * A command that sleeps for a certain number of milliseconds.
 */
public class SleepCommand extends CommandSupport
{
    private Semaphore terminatedSemaphore = new Semaphore(0);

    public SleepCommand(SleepCommandConfiguration config)
    {
        super(config);
    }

    @Override
    public SleepCommandConfiguration getConfig()
    {
        return (SleepCommandConfiguration) super.getConfig();
    }

    public void execute(CommandContext commandContext)
    {
        try
        {
            if (terminatedSemaphore.tryAcquire(getConfig().getInterval(), TimeUnit.MILLISECONDS))
            {
                commandContext.error("Terminated");
            }
        }
        catch (InterruptedException e)
        {
            // Empty
        }
    }

    public void terminate()
    {
        terminatedSemaphore.release();
    }
}
