package com.zutubi.pulse.core.commands.core;

import com.zutubi.pulse.core.commands.api.CommandContext;

/**
 */
public class NamedArgumentCommandSupport<T extends NamedArgumentCommandConfigurationSupport> extends ExecutableCommand<T>
{
    public NamedArgumentCommandSupport(T configuration)
    {
        super(configuration);
    }

    @Override
    public void execute(CommandContext commandContext)
    {
        for (NamedArgumentCommandConfigurationSupport.NamedArgument arg: getConfig().getNamedArguments())
        {
            commandContext.addCommandProperty(arg.getName(), arg.getValue());
        }
        
        super.execute(commandContext);
    }
}
