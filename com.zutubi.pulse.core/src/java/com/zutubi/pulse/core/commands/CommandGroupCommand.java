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

package com.zutubi.pulse.core.commands;

import com.zutubi.pulse.core.commands.api.Command;
import com.zutubi.pulse.core.commands.api.CommandContext;
import com.zutubi.pulse.core.commands.api.CommandSupport;

/**
 * Backwards-compatible wrapping command, which supports the old style of
 * artifact capture (prior to all commands supporting nested artifacts).
 */
public class CommandGroupCommand extends CommandSupport
{
    private CommandFactory commandFactory;

    /**
     * Constructor that stores the configuration for later access via
     * {@link #getConfig()}.
     *
     * @param config the configuration for this command
     */
    public CommandGroupCommand(CommandGroupConfiguration config)
    {
        super(config);
    }

    public void execute(CommandContext commandContext)
    {
        CommandGroupConfiguration config = (CommandGroupConfiguration) getConfig();
        Command command = commandFactory.create(config.getCommand());
        command.execute(commandContext);
    }

    public void setCommandFactory(CommandFactory commandFactory)
    {
        this.commandFactory = commandFactory;
    }
}
