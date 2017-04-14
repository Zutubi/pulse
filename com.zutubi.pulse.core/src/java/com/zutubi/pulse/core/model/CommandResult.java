/* Copyright 2017 Zutubi Pty Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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

    @Override
    public void complete(long endTime)
    {
        if (hasArtifactMessages(Feature.Level.WARNING))
        {
            completionState = ResultState.getWorseState(completionState, ResultState.WARNINGS);
        }
        
        super.complete(endTime);
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
        completionState = other.completionState;
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
