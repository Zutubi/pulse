package com.zutubi.pulse.core.commands.core;

import com.zutubi.pulse.core.Command;
import com.zutubi.pulse.core.commands.api.OutputProducingCommandTestCase;
import com.zutubi.pulse.core.commands.api.TestCommandContext;
import com.zutubi.pulse.core.engine.api.ResultState;

public abstract class ExecutableCommandTestCase extends OutputProducingCommandTestCase
{
    protected TestCommandContext failedRun(Command command, String ...contents) throws Exception
    {
        return stateRun(ResultState.FAILURE, command, contents);
    }

    protected TestCommandContext successRun(Command command, String ...contents) throws Exception
    {
        return stateRun(ResultState.SUCCESS, command, contents);
    }

    private TestCommandContext stateRun(ResultState expectedState, Command command, String... contents) throws Exception
    {
        TestCommandContext context = runCommand(command);
        assertEquals(expectedState, context.getResultState());
        assertOutputContains(contents);
        return context;
    }
}
