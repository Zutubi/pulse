package com.zutubi.pulse.core.commands.api;

import com.zutubi.pulse.core.Command;
import com.zutubi.tove.annotations.SymbolicName;

/**
 */
@SymbolicName("zutubi.buildingCommandConfigSupport")
public class BuildingCommandConfigurationSupport extends CommandConfigurationSupport
{
    private Class<? extends Command> commandType;

    public BuildingCommandConfigurationSupport(Class<? extends Command> commandType)
    {
        this.commandType = commandType;
    }

    public Command createCommand()
    {
        return buildCommand(commandType);
    }
}
