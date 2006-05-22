/********************************************************************************
 @COPYRIGHT@
 ********************************************************************************/
package com.zutubi.pulse.model;

import com.zutubi.pulse.core.model.Entity;

import java.util.LinkedList;
import java.util.List;

/**
 * A node in the BuildSpecification describes a single recipe to be executed
 * on a single host.
 */
public class BuildSpecificationNode extends Entity
{
    private BuildStage stage;
    private List<BuildSpecificationNode> children = new LinkedList<BuildSpecificationNode>();

    public BuildSpecificationNode()
    {

    }

    public BuildSpecificationNode(BuildStage stage)
    {
        this.stage = stage;
    }

    public BuildSpecificationNode copy()
    {
        BuildSpecificationNode copy = new BuildSpecificationNode();
        if(stage == null)
        {
            copy.stage = null;
        }
        else
        {
            copy.stage = stage.copy();
        }
        
        copy.children = new LinkedList<BuildSpecificationNode>();
        for(BuildSpecificationNode child: children)
        {
            copy.children.add(child.copy());
        }

        return copy;
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

    public BuildStage getStage()
    {
        return stage;
    }

    public void setStage(BuildStage stage)
    {
        this.stage = stage;
    }

}
