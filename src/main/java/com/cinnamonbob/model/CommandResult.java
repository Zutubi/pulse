package com.cinnamonbob.model;

import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import com.cinnamonbob.util.TimeStamps;

/**
 * 
 *
 */
public class CommandResult extends Entity
{
    private String commandName;
    private TimeStamps stamps;
    private boolean succeeded;
    private Properties properties;
    private List<StoredArtifact> artifacts =  new LinkedList<StoredArtifact>();;

    public CommandResult()
    {
        
    }
    
    public String getCommandName()
    {
        return commandName;
    }

    public void setCommandName(String name)
    {
        this.commandName = name;
    }
    
    public TimeStamps getStamps()
    {
        return stamps;
    }
    
    public boolean succeeded()
    {
        return succeeded;
    }
    
    public void addArtifact(StoredArtifact artifact)
    {
        artifacts.add(artifact);
    }
    
    public StoredArtifact getArtifact(String name)
    {
        for(StoredArtifact a: artifacts)
        {
            if(a.getName().equals(name))
            {
                return a;
            }
        }
        
        return null;
    }
    
    public List<StoredArtifact> getArtifacts()
    {
        return artifacts;
    }
    
    private void setArtifacts(List<StoredArtifact> artifacts)
    {
        this.artifacts = artifacts;
    }
    
    private boolean isSucceeded()
    {
        return succeeded;
    }
    
    public void setSucceeded(boolean succeeded)
    {
        this.succeeded = succeeded;
    }

    public void setStamps(TimeStamps stamps)
    {
        this.stamps = stamps;
    }
    
    public Properties getProperties()
    {
        if (properties == null)
        {
            properties = new Properties();
        }
        return properties;
    }
    
    private void setProperties(Properties properties)
    {
        this.properties = properties;
    }
}
