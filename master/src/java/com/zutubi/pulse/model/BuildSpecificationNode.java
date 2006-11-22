package com.zutubi.pulse.model;

import com.zutubi.pulse.core.model.Entity;
import com.zutubi.pulse.util.Predicate;

import java.util.Iterator;
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
    private List<ResourceRequirement> resourceRequirements = new LinkedList<ResourceRequirement>();
    private List<PostBuildAction> postActions = new LinkedList<PostBuildAction>();

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

        copy.resourceRequirements = new LinkedList<ResourceRequirement>();
        for(ResourceRequirement r: resourceRequirements)
        {
            copy.resourceRequirements.add(r.copy());
        }

        copy.children = new LinkedList<BuildSpecificationNode>();
        for(BuildSpecificationNode child: children)
        {
            copy.children.add(child.copy());
        }

        copy.postActions = new LinkedList<PostBuildAction>();
        for(PostBuildAction action: postActions)
        {
            copy.postActions.add(action.copy());
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

    public BuildSpecificationNode getNodeByPredicate(Predicate<BuildSpecificationNode> predicate)
    {
        if(predicate.satisfied(this))
        {
            return this;
        }

        for(BuildSpecificationNode child: children)
        {
            BuildSpecificationNode found = child.getNodeByPredicate(predicate);
            if(found != null)
            {
                return found;
            }
        }

        return null;
    }

    public void getNodesByPredicate(Predicate<BuildSpecificationNode> predicate, List<BuildSpecificationNode> nodes)
    {
        if(predicate.satisfied(this))
        {
            nodes.add(this);
        }

        for(BuildSpecificationNode child: children)
        {
            child.getNodesByPredicate(predicate, nodes);
        }
    }

    public BuildSpecificationNode getNode(final long id)
    {
        return getNodeByPredicate(new Predicate<BuildSpecificationNode>()
        {
            public boolean satisfied(BuildSpecificationNode t)
            {
                return t.getId() == id;
            }
        });
    }

    public BuildSpecificationNode getNodeByStageName(final String name)
    {
        return getNodeByPredicate(new Predicate<BuildSpecificationNode>()
        {
            public boolean satisfied(BuildSpecificationNode t)
            {
                return t.stage != null && name.equals(t.stage.getName());
            }
        });
    }

    public void removeChild(long id)
    {
        BuildSpecificationNode deadMan = null;

        for(BuildSpecificationNode child: children)
        {
            if(child.getId() == id)
            {
                deadMan = child;
                break;
            }
        }

        if(deadMan != null)
        {
            children.remove(deadMan);
        }
    }

    public List<ResourceRequirement> getResourceRequirements()
    {
        return resourceRequirements;
    }

    public void setResourceRequirements(List<ResourceRequirement> resourceRequirements)
    {
        this.resourceRequirements = resourceRequirements;
    }

    public void addResourceRequirement(ResourceRequirement resourceRequirement)
    {
        resourceRequirements.add(resourceRequirement);
    }

    public List<PostBuildAction> getPostActions()
    {
        return postActions;
    }

    private void setPostActions(List<PostBuildAction> postActions)
    {
        this.postActions = postActions;
    }

    public void addPostAction(PostBuildAction action)
    {
        postActions.add(action);
    }

    public void removePostAction(long id)
    {
        Iterator<PostBuildAction> it = postActions.iterator();
        while(it.hasNext())
        {
            if(it.next().getId() == id)
            {
                it.remove();
                return;
            }
        }
    }

    public PostBuildAction getPostAction(String name)
    {
        for(PostBuildAction p: postActions)
        {
            if(p.getName().equals(name))
            {
                return p;
            }
        }

        return null;
    }

    public PostBuildAction getPostAction(long id)
    {
        for(PostBuildAction a: postActions)
        {
            if(a.getId() == id)
            {
                return a;
            }
        }

        return null;
    }

}
