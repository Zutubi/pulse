package com.zutubi.pulse.web.project;

import com.zutubi.pulse.core.model.ResourceProperty;
import com.zutubi.pulse.model.BuildSpecification;
import com.zutubi.pulse.model.NamedEntityComparator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 */
public class ViewBuildSpecificationAction extends ProjectActionSupport
{
    private BuildSpecification specification;
    private long id;
    private long selectedNode;
    /**
     * Set to true when coming from ForceCleanBuildAction.
     */
    private boolean clean;
    private List<ResourceProperty> properties;

    public BuildSpecification getSpecification()
    {
        return specification;
    }

    public void setSpecification(BuildSpecification specification)
    {
        this.specification = specification;
    }

    public long getId()
    {
        return id;
    }

    public void setId(long id)
    {
        this.id = id;
    }

    public long getSelectedNode()
    {
        return selectedNode;
    }

    public void setSelectedNode(long selectedNode)
    {
        this.selectedNode = selectedNode;
    }

    public boolean haveSelectedNode()
    {
        return selectedNode != 0L && selectedNode != specification.getRoot().getId();
    }

    public boolean isClean()
    {
        return clean;
    }

    public void setClean(boolean clean)
    {
        this.clean = clean;
    }

    public List<ResourceProperty> getProperties()
    {
        return properties;
    }

    public String execute()
    {
        lookupProject(projectId);
        if (hasErrors())
        {
            return ERROR;
        }

        specification = getProject().getBuildSpecification(id);
        if (specification == null)
        {
            addActionError("Unknown build specification [" + id + "]");
            return ERROR;
        }

        properties = new ArrayList<ResourceProperty>(specification.getProperties());
        Collections.sort(properties, new NamedEntityComparator());

        return SUCCESS;
    }
}
