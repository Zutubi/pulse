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
