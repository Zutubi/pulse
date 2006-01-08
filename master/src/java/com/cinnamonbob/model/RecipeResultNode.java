package com.cinnamonbob.model;

import com.cinnamonbob.core.model.Entity;
import com.cinnamonbob.core.model.Feature;
import com.cinnamonbob.core.model.RecipeResult;

import java.util.LinkedList;
import java.util.List;

/**
 */
public class RecipeResultNode extends Entity
{
    private String host;
    private RecipeResult result;
    private List<RecipeResultNode> children;

    public RecipeResultNode()
    {
    }

    public RecipeResultNode(RecipeResult result)
    {
        this.result = result;
        children = new LinkedList<RecipeResultNode>();
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
}
