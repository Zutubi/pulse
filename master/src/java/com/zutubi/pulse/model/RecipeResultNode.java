package com.zutubi.pulse.model;

import com.zutubi.pulse.core.model.*;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

/**
 */
public class RecipeResultNode extends Entity
{
    private long stageHandle;
    private String stageName;
    private String host;
    private RecipeResult result;
    private List<RecipeResultNode> children;

    public RecipeResultNode()
    {
    }

    public RecipeResultNode(String stageName, long stageHandle, RecipeResult result)
    {
        this.stageName = stageName;
        this.stageHandle = stageHandle;
        this.result = result;
        children = new LinkedList<RecipeResultNode>();
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
     * WARNING: This stage configuration id may or MAY NOT exist.
     *  
     * @return
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

    public String getHost()
    {
        return host;
    }

    public void setHost(String host)
    {
        this.host = host;
    }

    public String getHostSafe()
    {
        if (host == null)
        {
            return "[pending]";
        }
        else
        {
            return host;
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

    public List<RecipeResultNode> getChildren()
    {
        return children;
    }

    private void setChildren(List<RecipeResultNode> children)
    {
        this.children = children;
    }

    public void addChild(RecipeResultNode child)
    {
        children.add(child);
    }

    public void abort()
    {
        if (!result.completed())
        {
            result.error("Recipe aborted");
            result.abortUnfinishedCommands();
            result.complete();
        }

        for (RecipeResultNode child : children)
        {
            child.abort();
        }
    }

    public List<String> collectErrors()
    {
        List<String> errors = new LinkedList<String>();
        errors.addAll(result.collectErrors());
        for (RecipeResultNode child : children)
        {
            errors.addAll(child.collectErrors());
        }

        return errors;
    }

    public boolean hasMessages(Feature.Level level)
    {
        if (result.hasMessages(level))
        {
            return true;
        }

        for (RecipeResultNode child : children)
        {
            if (child.hasMessages(level))
            {
                return true;
            }
        }

        return false;
    }

    public boolean hasArtifacts()
    {
        if (result.hasArtifacts())
        {
            return true;
        }

        for (RecipeResultNode child : children)
        {
            if (child.hasArtifacts())
            {
                return true;
            }
        }

        return false;
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
        if (result != null)
        {
            result.accumulateTestSummary(summary);
        }

        for (RecipeResultNode r : children)
        {
            r.accumulateTestSummary(summary);
        }
    }

    public RecipeResultNode findNode(long id)
    {
        if(id == getId())
        {
            return this;
        }

        for(RecipeResultNode child: children)
        {
            RecipeResultNode found = child.findNode(id);
            if(found != null)
            {
                return found;
            }
        }

        return null;
    }

    public RecipeResultNode findNodeByHandle(long handle)
    {
        if(handle == getStageHandle())
        {
            return this;
        }

        for(RecipeResultNode child: children)
        {
            RecipeResultNode found = child.findNodeByHandle(handle);
            if(found != null)
            {
                return found;
            }
        }

        return null;
    }

    public RecipeResultNode findNode(String stageName)
    {
        if(this.stageName != null && stageName.equals(this.stageName))
        {
            return this;
        }

        for(RecipeResultNode child: children)
        {
            RecipeResultNode found = child.findNode(stageName);
            if(found != null)
            {
                return found;
            }
        }
        return null;
    }

    public ResultState getWorstState(ResultState worst)
    {
        if(result != null)
        {
            worst = ResultState.getWorseState(worst, result.getState());
        }

        for(RecipeResultNode child: children)
        {
            worst = child.getWorstState(worst);
        }

        return worst;
    }

    public void loadFeatures(File dataRoot)
    {
        if(result != null)
        {
            result.loadFeatures(dataRoot);
        }

        for(RecipeResultNode child: children)
        {
            child.loadFeatures(dataRoot);
        }
    }

    public void loadFailedTestResults(File dataRoot, int limit)
    {
        if(result != null)
        {
            result.loadFailedTestResults(dataRoot, limit);
        }

        for(RecipeResultNode child: children)
        {
            child.loadFailedTestResults(dataRoot, limit);
        }
    }

    public StoredArtifact findArtifact(String artifactName)
    {
        StoredArtifact artifact = result.getArtifact(artifactName);
        if (artifact != null)
        {
            return artifact;
        }
        for(RecipeResultNode child: children)
        {
            artifact = child.findArtifact(artifactName);
            if (artifact != null)
            {
                return artifact;
            }
        }
        return null;
    }
}
