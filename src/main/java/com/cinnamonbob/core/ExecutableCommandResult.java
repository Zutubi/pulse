package com.cinnamonbob.core;



/**
 * Encapsulates the result of running a generic executable.
 * 
 * @author jsankey
 */
public class ExecutableCommandResult implements CommandResult
{
    private static final int EXIT_SUCCESS = 0;
    
    private String commandLine;
    private String workingDir;
    private int exitCode;
    
    
    public ExecutableCommandResult(String commandLine, String workingDir, int exitCode)
    {
        this.commandLine = commandLine;
        this.workingDir = workingDir;
        this.exitCode   = exitCode;
    }
    
    /**
     * @see com.cinnamonbob.core.CommandResult#succeeded()
     */
    public boolean succeeded()
    {
        return exitCode == EXIT_SUCCESS;
    }

    /**
     * @see com.cinnamonbob.core.CommandResult#getSummary()
     */
    public String getSummary()
    {
        return "Child exited with code " + Integer.toString(exitCode) + ".";
    }
    
    /**
     * @see com.cinnamonbob.core.CommandResult#changedBy(java.lang.String)
     */
    public boolean changedBy(String login)
    {
        return false;
    }

    /**
     * @return Returns the command line that was run.
     */
    public String getCommandLine()
    {
        return commandLine;
    }
    
    /**
     * @return Returns the working directory the command was executed in.
     */
    public String getWorkingDir()
    {
        return workingDir;
    }
    
    /**
     * @return Returns the exitCode.
     */
    public int getExitCode()
    {
        return exitCode;
    }
}
