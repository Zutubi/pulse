package com.cinnamonbob.core;

import java.util.LinkedList;
import java.util.List;

public class BuildSnapshot
{
    private int                   id;
    private boolean               completed;
    private TimeStamps            stamps;
    private String                currentCommand;
    private List<CommandSnapshot> completedCommands;
    
    public BuildSnapshot(int id, boolean completed, TimeStamps stamps, String currentCommand, List<CommandResultCommon> completedCommands)
    {
        this.id = id;
        this.completed = completed;
        this.stamps = stamps;
        this.currentCommand = currentCommand;
        this.completedCommands = new LinkedList<CommandSnapshot>();
        
        for(CommandResultCommon result: completedCommands)
        {
            this.completedCommands.add(new CommandSnapshot(result.getCommandName(), result.getStamps(), result.getResult().succeeded()));
        }
    }

    public boolean isCompleted()
    {
        return completed;
    }
    

    public List<CommandSnapshot> getCompletedCommands()
    {
        return completedCommands;
    }
    

    public String getCurrentCommand()
    {
        return currentCommand;
    }
    

    public int getId()
    {
        return id;
    }
    

    public TimeStamps getStamps()
    {
        return stamps;
    }
    
}
