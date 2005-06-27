package com.cinnamonbob.core2.config;

import junit.framework.TestCase;

/**
 * 
 *
 */
public class ExecutableCommandTest extends TestCase
{
    public void testExecuteSuccessExpected() throws Exception
    {
        ExecutableCommand command = new ExecutableCommand();
        command.setExe("dir");
        command.setArgs(".");
        CommandResult result = command.execute();
        assertTrue(result.succeeded());        
    }
    
    public void testExecuteFailureExpected() throws Exception
    {
        ExecutableCommand command = new ExecutableCommand();
        command.setExe("dir");
        command.setArgs("w");
        CommandResult result = command.execute();
        assertFalse(result.succeeded());        
    }

    public void testExecuteSuccessExpectedNoArg() throws Exception
    {
        ExecutableCommand command = new ExecutableCommand();
        command.setExe("netstat");
        CommandResult result = command.execute();
        assertTrue(result.succeeded());        
    }
    
    public void testExecuteExceptionExpected() throws Exception
    {
        ExecutableCommand command = new ExecutableCommand();
        command.setExe("unknown");
        command.setArgs("command");
        try 
        {
            command.execute();
            assertTrue(false);
        } catch (CommandException e)
        {
            // noop            
        }
    }
}
