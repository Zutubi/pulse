package com.zutubi.pulse.master.model;

import com.zutubi.pulse.core.engine.api.Feature;
import com.zutubi.pulse.core.model.Entity;
import com.zutubi.pulse.core.model.RecipeResult;
import com.zutubi.pulse.core.model.StoredArtifact;
import com.zutubi.pulse.core.model.TestResultSummary;
import com.zutubi.pulse.master.tove.config.project.BuildStageConfiguration;

import java.io.File;
import java.util.List;

/**
 */
public class RecipeResultNode extends Entity
{
    private long stageHandle;
    private String stageName;
    private String agentName;
    private RecipeResult result;

    public RecipeResultNode()
    {
    }

    public RecipeResultNode(BuildStageConfiguration stage, RecipeResult result)
    {
        this.stageName = stage.getName();
        this.stageHandle = stage.getHandle();
        this.result = result;
    }
    
    public RecipeResultNode(String stageName, long stageHandle, RecipeResult result)
    {
        this.stageName = stageName;
        this.stageHandle = stageHandle;
        this.result = result;
    }

    public String getStageName()
    {
        return stageName;
    }

    /**
     * Used by hibernate
     */
    private void setStageName(String stageName)
    {
        this.stageName = stageName;
    }

    /**
     * WARNING: The actual stage configuration for this handle may not exist.
     * It may have been deleted after this build occured.
     *  
     * @return a handle that can be used to identify the same stage across
     *         multiple builds
     */
    public long getStageHandle()
    {
        return stageHandle;
    }

    /**
     * Used by hibernate
     */
    private void setStageHandle(long stageHandle)
    {
        this.stageHandle = stageHandle;
    }

    public String getAgentName()
    {
        return agentName;
    }

    public void setAgentName(String agentName)
    {
        this.agentName = agentName;
    }

    public String getAgentNameSafe()
    {
        if (agentName == null)
        {
            return "[pending]";
        }
        else
        {
            return agentName;
        }
    }

    public RecipeResult getResult()
    {
        return result;
    }

    private void setResult(RecipeResult result)
    {
        this.result = result;
    }

    public void abort()
    {
        if (!result.completed())
        {
            result.error("Recipe aborted");
            result.abortUnfinishedCommands();
            result.complete();
        }
    }

    public List<String> collectErrors()
    {
        return result.collectErrors();
    }

    public boolean hasMessages(Feature.Level level)
    {
        return result.hasMessages(level);
    }

    public boolean hasArtifacts()
    {
        return result.hasArtifacts();
    }

    public boolean hasBrokenTests()
    {
        return getTestSummary().getBroken() > 0;
    }

    public TestResultSummary getTestSummary()
    {
        TestResultSummary summary = new TestResultSummary();
        accumulateTestSummary(summary);
        return summary;
    }

    public void accumulateTestSummary(TestResultSummary summary)
    {
        result.accumulateTestSummary(summary);
    }

    public void loadFeatures(File dataRoot)
    {
        result.loadFeatures(dataRoot);
    }

    public void loadFailedTestResults(File dataRoot, int limit)
    {
        result.loadFailedTestResults(dataRoot, limit);
    }

    public StoredArtifact findArtifact(String artifactName)
    {
        return result.getArtifact(artifactName);
    }
}
