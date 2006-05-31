package com.zutubi.pulse.model;

import com.zutubi.pulse.BuildService;

import java.util.List;

/**
 */
public class AnyCapableBuildHostRequirements extends AbstractBuildHostRequirements
{
    private BuildSpecification specification;
    private BuildSpecificationNode node;

    public AnyCapableBuildHostRequirements(BuildSpecification spec, BuildSpecificationNode node)
    {
        this.specification = spec;
        this.node = node;
    }

    public BuildHostRequirements copy()
    {
        return new AnyCapableBuildHostRequirements(specification, node);
    }

    public boolean fulfilledBy(BuildService service)
    {
        return requirementsMet(specification.getRoot(), service) && requirementsMet(node, service);
    }

    private boolean requirementsMet(BuildSpecificationNode node, BuildService service)
    {
        List<ResourceRequirement> requirements = node.getResourceRequirements();
        for(ResourceRequirement requirement: requirements)
        {
            if(!service.hasResource(requirement.getResource(), requirement.getVersion()))
            {
                return false;
            }
        }
        return true;
    }

    public String getSummary()
    {
        return "[any]";
    }

    public BuildSpecification getSpecification()
    {
        return specification;
    }

    private void setSpecification(BuildSpecification specification)
    {
        this.specification = specification;
    }

    public BuildSpecificationNode getNode()
    {
        return node;
    }

    private void setNode(BuildSpecificationNode node)
    {
        this.node = node;
    }
}
