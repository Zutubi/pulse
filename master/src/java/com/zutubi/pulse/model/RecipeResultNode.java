package com.zutubi.pulse.model;

import com.zutubi.pulse.core.model.*;

import java.util.LinkedList;
import java.util.List;

/**
 */
public class RecipeResultNode extends Entity
{
    private String stage;
    private String host;
    private RecipeResult result;
    private List<RecipeResultNode> children;

    public RecipeResultNode()
    {
    }

    public RecipeResultNode(String stage, RecipeResult result)
    {
        this.stage = stage;
        this.result = result;
        children = new LinkedList<RecipeResultNode>();
    }

    public String getStage()
    {
        return stage;
    }

    public void setStage(String stage)
    {
        this.stage = stage;
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

    public RecipeResultNode findNode(String stage)
    {
        if(stage.equals(this.stage))
        {
            return this;
        }

        for(RecipeResultNode child: children)
        {
            RecipeResultNode found = child.findNode(stage);
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

}
