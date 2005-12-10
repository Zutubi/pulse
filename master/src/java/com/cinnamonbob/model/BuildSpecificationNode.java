package com.cinnamonbob.model;

import com.cinnamonbob.core.model.Entity;

import java.util.List;
import java.util.LinkedList;

/**
 * A node in the BuildSpecification describes a single recipe to be executed
 * on a single host.
 */
public class BuildSpecificationNode extends Entity
{
    private BuildServiceResolver serviceResolver;
    private String recipe;
    private List<BuildSpecificationNode> children = new LinkedList<BuildSpecificationNode>();

    public BuildSpecificationNode()
    {

    }

    public BuildSpecificationNode(BuildServiceResolver resolver, String recipe)
    {
        this.serviceResolver = resolver;
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

    private void setRecipe(String recipe)
    {
        this.recipe = recipe;
    }

    public BuildServiceResolver getServiceResolver()
    {
        return serviceResolver;
    }

    private void setServiceResolver(BuildServiceResolver serviceResolver)
    {
        this.serviceResolver = serviceResolver;
    }
}
