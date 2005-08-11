package com.cinnamonbob.core.config;

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
        outputDirectory = FileSystemUtils.createTmpDirectory(ExecutableCommandTest.class.getName(), "");
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
        CommandResult result = new CommandResult();
        command.execute(outputDirectory, result);
        assertEquals(result.getState(), ResultState.SUCCESS);        
    }
    
    public void testExecuteFailureExpected() throws Exception
    {
        ExecutableCommand command = new ExecutableCommand();
        command.setExe("dir");
        command.setArgs("w");
        CommandResult result = new CommandResult();
        command.execute(outputDirectory, result);
        assertEquals(result.getState(), ResultState.FAILURE);        
    }

    public void testExecuteSuccessExpectedNoArg() throws Exception
    {
        ExecutableCommand command = new ExecutableCommand();
        command.setExe("netstat");
        CommandResult result = new CommandResult();
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
            command.execute(outputDirectory, new CommandResult());
            assertTrue(false);
        } catch (BuildException e)
        {
            // noop            
        }
    }
}
