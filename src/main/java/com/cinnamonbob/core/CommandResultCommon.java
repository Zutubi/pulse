package com.cinnamonbob.core;



/**
 * @author jsankey
 */
public class CommandResultCommon
{
    private String        commandName;
    private TimeStamps    stamps;
    private CommandResult result;
    
    
    public CommandResultCommon(String commandName, CommandResult result, TimeStamps stamps)
    {
        this.commandName = commandName;
        this.result = result;
        this.stamps = stamps;
    }
    
    
    /**
     * Returns the name of the command that generated this result.
     * 
     * @return the command executed to generate this result
     */
    public String getCommandName()
    {
        return commandName;
    }
    
    /**
     * Returns the nested command result.
     * 
     * @return the actual result of the command
     */
    public CommandResult getResult()
    {
        return result;
    }
    
    /**
     * Returns the timestamps for this result.
     * 
     * @return stamps indicating the start and finish time of the command
     */
    public TimeStamps getStamps()
    {
        return stamps;
    }
}
