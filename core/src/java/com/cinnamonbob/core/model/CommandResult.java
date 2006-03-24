package com.cinnamonbob.core.model;

import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

/**
 * 
 *
 */
public class CommandResult extends Result
{
    private String commandName;
    private Properties properties;
    private List<StoredArtifact> artifacts = new LinkedList<StoredArtifact>();

    protected CommandResult()
    {

    }

    public CommandResult(String name)
    {
        commandName = name;
        state = ResultState.INITIAL;
    }

    public String getCommandName()
    {
        return commandName;
    }

    private void setCommandName(String name)
    {
        this.commandName = name;
    }

    public void addArtifact(StoredArtifact artifact)
    {
        artifacts.add(artifact);
    }

    public StoredArtifact getArtifact(String name)
    {
        for (StoredArtifact a : artifacts)
        {
            if (a.getName().equals(name))
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

    public boolean hasMessages(Feature.Level level)
    {
        if (hasDirectMessages(level))
        {
            return true;
        }

        for (StoredArtifact artifact : artifacts)
        {
            if (artifact.hasMessages(level))
            {
                return true;
            }
        }

        return false;
    }

    public boolean hasArtifacts()
    {
        return artifacts.size() > 0;
    }

    public void accumulateTestSummary(TestResultSummary summary)
    {
        for (StoredArtifact a : artifacts)
        {
            a.accumulateTestSummary(summary);
        }
    }
}
