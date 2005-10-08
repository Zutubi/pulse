package com.cinnamonbob.core;

import com.cinnamonbob.core.BuildException;
import com.cinnamonbob.model.CommandResult;
import com.cinnamonbob.model.ResultState;
import com.cinnamonbob.util.FileSystemUtils;
import junit.framework.TestCase;

import java.io.File;

/**
 * 
 *
 */
public class ExecutableCommandTest extends TestCase
{
        
    private File outputDirectory; 
    
    public void setUp() throws Exception
    {
        super.setUp();
        outputDirectory = FileSystemUtils.createTempDirectory(ExecutableCommandTest.class.getName(), "");
    }
    
    public void tearDown() throws Exception
    {
        FileSystemUtils.removeDirectory(outputDirectory);        
        super.tearDown();
    }
    
    public void testExecuteSuccessExpected() throws Exception
    {
        ExecutableCommand command = new ExecutableCommand();
        command.setExe("dir");
        command.setArgs(".");
        CommandResult result = new CommandResult("success");
        command.execute(outputDirectory, result);
        assertEquals(result.getState(), ResultState.SUCCESS);        
    }
    
    public void testExecuteFailureExpected() throws Exception
    {
        ExecutableCommand command = new ExecutableCommand();
        command.setExe("dir");
        command.setArgs("w");
        CommandResult result = new CommandResult("failure");
        command.execute(outputDirectory, result);
        assertEquals(result.getState(), ResultState.FAILURE);        
    }

    public void testExecuteSuccessExpectedNoArg() throws Exception
    {
        ExecutableCommand command = new ExecutableCommand();
        command.setExe("netstat");
        CommandResult result = new CommandResult("no arg");
        command.execute(outputDirectory, result);
        assertEquals(result.getState(), ResultState.SUCCESS);        
    }
    
    public void testExecuteExceptionExpected() throws Exception
    {
        ExecutableCommand command = new ExecutableCommand();
        command.setExe("unknown");
        command.setArgs("command");
        try 
        {
            command.execute(outputDirectory, new CommandResult("exception"));
            assertTrue(false);
        } catch (BuildException e)
        {
            // noop            
        }
    }
}
