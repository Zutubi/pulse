package com.cinnamonbob.model;

import com.cinnamonbob.core.model.Entity;

import java.util.LinkedList;
import java.util.List;

/**
 * A node in the BuildSpecification describes a single recipe to be executed
 * on a single host.
 */
public class BuildSpecificationNode extends Entity
{
    private BuildHostRequirements hostRequirements;
    private String recipe;
    private List<BuildSpecificationNode> children = new LinkedList<BuildSpecificationNode>();

    public BuildSpecificationNode()
    {

    }

    public BuildSpecificationNode(BuildHostRequirements resolver, String recipe)
    {
        this.hostRequirements = resolver;
        this.recipe = recipe;
    }

    public List<BuildSpecificationNode> getChildren()
    {
        return children;
    }

    private void setChildren(List<BuildSpecificationNode> children)
    {
        this.children = children;
    }

    public void addChild(BuildSpecificationNode child)
    {
        children.add(child);
    }

    public String getRecipe()
    {
        return recipe;
    }

    public void setRecipe(String recipe)
    {
        this.recipe = recipe;
    }

    public BuildHostRequirements getHostRequirements()
    {
        return hostRequirements;
    }

    public void setHostRequirements(BuildHostRequirements hostRequirements)
    {
        this.hostRequirements = hostRequirements;
    }
}
