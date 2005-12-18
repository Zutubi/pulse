package com.cinnamonbob.model;

import com.cinnamonbob.core.model.Entity;
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
}
