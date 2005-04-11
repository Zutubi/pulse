package com.cinnamonbob.core;

import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;



/**
 * @author jsankey
 */
public class CommandResultCommon
{
    private String        commandName;
    private TimeStamps    stamps;
    private CommandResult result;
    private Map<String, Artifact> artifacts;
    
    
    public CommandResultCommon(String commandName, CommandResult result, TimeStamps stamps)
    {
        this.commandName = commandName;
        this.result = result;
        this.stamps = stamps;
        artifacts = new TreeMap<String, Artifact>();
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
    
    
    /**
     * Adds the given artifact to this result.
     * 
     * @param artifact the artifact to add
     */
    public void addArtifact(Artifact artifact)
    {
        artifacts.put(artifact.getName(), artifact);
    }


    public boolean hasArtifact(String name)
    {
        return artifacts.containsKey(name);
    }
    
    
    public Artifact getArtifact(String name)
    {
        return artifacts.get(name);
    }
    
    
    public Collection<Artifact> getArtifacts()
    {
        return artifacts.values();
    }
}
