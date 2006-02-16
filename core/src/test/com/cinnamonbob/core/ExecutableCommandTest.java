package com.cinnamonbob.core;

import com.cinnamonbob.core.model.CommandResult;
import com.cinnamonbob.core.model.ResultState;
import com.cinnamonbob.core.util.FileSystemUtils;
import junit.framework.TestCase;

import java.io.File;

/**
 * 
 *
 */
public class ExecutableCommandTest extends TestCase
{
    private File baseDirectory;
    private File outputDirectory;

    public void setUp() throws Exception
    {
        super.setUp();
        baseDirectory = FileSystemUtils.createTempDirectory(ExecutableCommandTest.class.getName(), ".base");
        outputDirectory = FileSystemUtils.createTempDirectory(ExecutableCommandTest.class.getName(), ".out");
    }

    public void tearDown() throws Exception
    {
        FileSystemUtils.removeDirectory(outputDirectory);
        FileSystemUtils.removeDirectory(baseDirectory);
        super.tearDown();
    }

    public void testExecuteSuccessExpected() throws Exception
    {
        ExecutableCommand command = new ExecutableCommand();
        command.setExe("dir");
        command.setArgs(".");
        CommandResult result = new CommandResult("success");
        command.execute(baseDirectory, outputDirectory, result);
        assertEquals(result.getState(), ResultState.SUCCESS);
    }

    public void testExecuteFailureExpected() throws Exception
    {
        ExecutableCommand command = new ExecutableCommand();
        command.setExe("dir");
        command.setArgs("w");
        CommandResult result = new CommandResult("failure");
        command.execute(baseDirectory, outputDirectory, result);
        assertEquals(result.getState(), ResultState.FAILURE);
    }

    public void testExecuteSuccessExpectedNoArg() throws Exception
    {
        ExecutableCommand command = new ExecutableCommand();
        command.setExe("netstat");
        CommandResult result = new CommandResult("no arg");
        command.execute(baseDirectory, outputDirectory, result);
        assertEquals(result.getState(), ResultState.SUCCESS);
    }

    public void testExecuteExceptionExpected() throws Exception
    {
        ExecutableCommand command = new ExecutableCommand();
        command.setExe("unknown");
        command.setArgs("command");
        try
        {
            command.execute(baseDirectory, outputDirectory, new CommandResult("exception"));
            assertTrue(false);
        }
        catch (BuildException e)
        {
            // noop            
        }
    }
}
