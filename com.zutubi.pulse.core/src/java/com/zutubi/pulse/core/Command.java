package com.zutubi.pulse.core;

import com.zutubi.pulse.core.commands.api.CommandContext;

/**
 * Basic interface for commands.  Commands are single units of work in a
 * recipe.  The most common example is launching an external build tool
 * (like ant or make) to build supplied targets.
 */
public interface Command
{
    /**
     * Execute the command, providing feedback via the given context.
     *
     * @param commandContext context used to provide feedback of the command's
     *                       result and register captures
     */
    void execute(CommandContext commandContext);

    /**
     * The terminate method allows the command's execution to be interupted.
     * It may be called at any time after a call to execute, from any thread.
     */
    void terminate();
}
