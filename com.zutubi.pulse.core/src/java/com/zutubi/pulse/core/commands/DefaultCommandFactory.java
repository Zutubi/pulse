package com.zutubi.pulse.core.commands;

import com.zutubi.pulse.core.ConfiguredInstanceFactory;
import com.zutubi.pulse.core.commands.api.Command;
import com.zutubi.pulse.core.commands.api.CommandConfiguration;

/**
 * Default implementation of {@link CommandFactory},
 * which uses the object factory to build commands.
 */
public class DefaultCommandFactory extends ConfiguredInstanceFactory<Command, CommandConfiguration> implements CommandFactory
{
    protected Class<? extends Command> getType(CommandConfiguration configuration)
    {
        return configuration.commandType();
    }
}