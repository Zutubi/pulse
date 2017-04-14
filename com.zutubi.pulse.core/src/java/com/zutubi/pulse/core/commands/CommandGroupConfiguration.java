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

import com.zutubi.pulse.core.commands.api.CommandConfiguration;
import com.zutubi.pulse.core.commands.api.CommandConfigurationSupport;
import com.zutubi.tove.annotations.Internal;
import com.zutubi.tove.annotations.SymbolicName;
import com.zutubi.util.StringUtils;

/**
 * Configuration for a wrapping &lt;command&gt; tag, purely for backwards-
 * compatibility with 2.0.  Marked as internal so it does not show up in
 * generated documentation.
 */
@SymbolicName("zutubi.commandGroupConfig")
@Internal
public class CommandGroupConfiguration extends CommandConfigurationSupport
{
    private CommandConfiguration command;

    public CommandGroupConfiguration()
    {
        super(CommandGroupCommand.class);
    }

    public CommandConfiguration getCommand()
    {
        return command;
    }

    public void setCommand(CommandConfiguration command)
    {
        // Equalise the names, preferring our own.
        String name = getName();
        if (StringUtils.stringSet(name))
        {
            command.setName(name);
        }
        else
        {
            setName(command.getName());
        }
        
        this.command = command;
    }
}
