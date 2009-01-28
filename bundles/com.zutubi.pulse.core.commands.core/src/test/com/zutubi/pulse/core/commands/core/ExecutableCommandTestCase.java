package com.zutubi.pulse.core.commands.core;

import com.zutubi.pulse.core.Command;
import com.zutubi.pulse.core.commands.api.OutputProducingCommandTestCase;
import com.zutubi.pulse.core.commands.api.TestCommandContext;
import com.zutubi.pulse.core.engine.api.ResultState;

import java.io.File;
import java.io.IOException;

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

    protected File copyBuildFile(String name, String extension, String toName) throws IOException
    {
        File buildFile = copyInputToDirectory(name, extension, baseDir);
        assertTrue(buildFile.renameTo(new File(baseDir, toName)));
        return buildFile;
    }
}
