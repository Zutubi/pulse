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

package com.zutubi.pulse.core;

import com.zutubi.pulse.core.commands.api.CommandContext;
import com.zutubi.pulse.core.commands.api.CommandSupport;

/**
 * A simple command that does no processing.
 */
public class NoopCommand extends CommandSupport
{
    private boolean executed;

    private boolean terminated;

    public NoopCommand(NoopCommandConfiguration config)
    {
        super(config);
    }

    public void execute(CommandContext commandContext)
    {
        executed = true;

        if (terminated)
        {
            commandContext.error("Command terminated");
        }
    }

    public boolean hasExecuted()
    {
        return executed;
    }

    public void terminate()
    {
        this.terminated = true;
    }

    public boolean isTerminated()
    {
        return terminated;
    }
}
