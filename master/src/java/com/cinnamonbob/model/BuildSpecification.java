package com.cinnamonbob.model;

import com.cinnamonbob.core.model.Entity;

import java.util.List;
import java.util.LinkedList;

/**
 * Describes the steps (recipes) required for a build, and where they should
 * be executed.
 */
public class BuildSpecification extends Entity
{
    private String name;
    private List<BuildSpecificationNode> nodes = new LinkedList<BuildSpecificationNode>();

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

    private void setName(String name)
    {
        this.name = name;
    }

    public List<BuildSpecificationNode> getNodes()
    {
        return nodes;
    }

    public void addNode(BuildSpecificationNode node)
    {
        nodes.add(node);
    }

    private void setNodes(List<BuildSpecificationNode> nodes)
    {
        this.nodes = nodes;
    }
}
