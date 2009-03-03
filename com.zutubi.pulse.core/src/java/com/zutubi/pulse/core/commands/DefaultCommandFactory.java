package com.zutubi.pulse.core.commands;

import com.zutubi.pulse.core.Command;
import com.zutubi.pulse.core.ConfiguredInstanceFactory;
import com.zutubi.pulse.core.commands.api.CommandConfiguration;
import com.zutubi.pulse.core.commands.api.CommandFactory;

/**
 * Default implementation of {@link com.zutubi.pulse.core.commands.api.CommandFactory},
 * which uses the object factory to build commands.
 */
public class DefaultCommandFactory extends ConfiguredInstanceFactory<Command, CommandConfiguration> implements CommandFactory
{
    protected Class<? extends Command> getType(CommandConfiguration configuration)
    {
        return configuration.commandType();
    }
}