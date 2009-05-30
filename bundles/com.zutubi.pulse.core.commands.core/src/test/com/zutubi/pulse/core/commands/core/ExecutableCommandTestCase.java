package com.zutubi.pulse.core.commands.core;

import com.zutubi.pulse.core.commands.api.Command;
import com.zutubi.pulse.core.commands.api.OutputProducingCommandTestCase;
import com.zutubi.pulse.core.commands.api.TestCommandContext;
import com.zutubi.pulse.core.engine.api.ResultState;
import com.zutubi.util.FileSystemUtils;

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

    protected TestCommandContext stateRun(ResultState expectedState, Command command, String... contents) throws Exception
    {
        TestCommandContext context = runCommand(command);
        assertEquals(expectedState, context.getResultState());
        assertDefaultOutputContains(contents);
        return context;
    }

    protected File copyBuildFile(String name, String extension, String toName) throws IOException
    {
        File buildFile = copyInputToDirectory(name, extension, baseDir);
        assertTrue(FileSystemUtils.robustRename(buildFile, new File(baseDir, toName)));
        return buildFile;
    }
}
