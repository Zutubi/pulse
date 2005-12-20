package com.cinnamonbob.model;

import com.cinnamonbob.core.model.Entity;

/**
 * Describes the steps (recipes) required for a build, and where they should
 * be executed.
 */
public class BuildSpecification extends Entity
{
    private String name;
    private BuildSpecificationNode root = new BuildSpecificationNode(null, null);

    public BuildSpecification()
    {

    }

    public BuildSpecification(String name)
    {
        this.name = name;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public BuildSpecificationNode getRoot()
    {
        return root;
    }

    public void setRoot(BuildSpecificationNode root)
    {
        this.root = root;
    }
}
