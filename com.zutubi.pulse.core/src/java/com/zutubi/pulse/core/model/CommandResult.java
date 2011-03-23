package com.zutubi.pulse.core.model;

import com.zutubi.pulse.core.engine.api.Feature;
import com.zutubi.pulse.core.engine.api.ResultState;
import com.zutubi.util.StringUtils;
import com.zutubi.util.logging.Logger;

import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

/**
 * 
 *
 */
public class CommandResult extends Result
{
    private static final Logger LOG = Logger.getLogger(CommandResult.class);
    
    public static final int FEATURE_LIMIT_PER_FILE = Integer.getInteger("pulse.feature.limit.per.file", 4096);

    // NOTE: if you add a field here (or to the base class) you must also
    // modify the update() method!
    private String commandName;
    private Properties properties;
    private List<StoredArtifact> artifacts = new LinkedList<StoredArtifact>();

    protected CommandResult()
    {

    }

    public CommandResult(String name)
    {
        commandName = name;
        state = ResultState.PENDING;
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

        if (hasArtifactMessages(level))
        {
            return true;
        }

        return false;
    }

    public boolean hasArtifactMessages(Feature.Level level)
    {
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

    public StoredFileArtifact getFileArtifact(String path)
    {
        String[] tokenised = StringUtils.getNextToken(path, '/', true);
        if(tokenised == null || tokenised[1].length() == 0)
        {
            return null;
        }

        StoredArtifact artifact = getArtifact(tokenised[0]);
        if(artifact == null)
        {
            return null;
        }

        return artifact.findFileBase(tokenised[1]);
    }

    public void calculateFeatureCounts()
    {
        super.calculateFeatureCounts();

        for (StoredArtifact artifact : artifacts)
        {
            warningFeatureCount += artifact.getFeatures(Feature.Level.WARNING).size();
            errorFeatureCount += artifact.getFeatures(Feature.Level.ERROR).size();
        }
    }

    public void loadFeatures(File recipeDir)
    {
        if (completed())
        {
            try
            {
                FeaturePersister persister = new FeaturePersister();
                persister.readFeatures(this, recipeDir, FEATURE_LIMIT_PER_FILE);
            }
            catch (Exception e)
            {
                LOG.severe("Unable to load features: " + e.getMessage(), e);
            }
        }
    }

    public void update(CommandResult other)
    {
        state = other.state;

        // Keep our own start time
        other.stamps.setStartTime(stamps.getStartTime());
        stamps = other.stamps;

        setOutputDir(other.getOutputDir());

        features.clear();
        features.addAll(other.features);

        warningFeatureCount = other.warningFeatureCount;
        errorFeatureCount = other.errorFeatureCount;

        commandName = other.commandName;

        getProperties().clear();
        properties.putAll(other.getProperties());

        artifacts.clear();
        artifacts.addAll(other.artifacts);
    }
}
