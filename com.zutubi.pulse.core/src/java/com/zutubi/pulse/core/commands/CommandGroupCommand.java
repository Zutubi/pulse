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
