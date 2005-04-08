package com.cinnamonbob.core;



/**
 * Encapsulates the result of running a generic executable.
 * 
 * @author jsankey
 */
public class ExecutableCommandResult implements CommandResult
{
    private static final int EXIT_SUCCESS = 0;
    
    private int exitCode;
    
    
    public ExecutableCommandResult(int exitCode)
    {
        this.exitCode = exitCode;
    }
    
    /* (non-Javadoc)
     * @see com.cinnamonbob.core.CommandResult#succeeded()
     */
    public boolean succeeded()
    {
        return exitCode == EXIT_SUCCESS;
    }

    /* (non-Javadoc)
     * @see com.cinnamonbob.core.CommandResult#getSummary()
     */
    public String getSummary()
    {
        return "Child exited with code " + Integer.toString(exitCode) + ".";
    }

    /**
     * @return Returns the exitCode.
     */
    public int getExitCode()
    {
        return exitCode;
    }
    
}
