package com.zutubi.pulse.core.commands.core;

import com.zutubi.pulse.core.commands.api.CommandContext;

/**
 */
public class NamedArgumentCommand extends ExecutableCommand
{
    public NamedArgumentCommand(NamedArgumentCommandConfiguration configuration)
    {
        super(configuration);
    }

    @Override
    public NamedArgumentCommandConfiguration getConfig()
    {
        return (NamedArgumentCommandConfiguration) super.getConfig();
    }

    @Override
    public void execute(CommandContext commandContext)
    {
        for (NamedArgumentCommandConfiguration.NamedArgument arg: getConfig().getNamedArguments())
        {
            commandContext.addCommandProperty(arg.getName(), arg.getValue());
        }
        
        super.execute(commandContext);
    }
}
