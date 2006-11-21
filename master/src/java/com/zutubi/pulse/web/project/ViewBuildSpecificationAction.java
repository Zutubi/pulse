package com.zutubi.pulse.web.project;

import com.zutubi.pulse.model.BuildSpecification;
import com.zutubi.pulse.model.Project;
import com.zutubi.pulse.model.NamedEntityComparator;
import com.zutubi.pulse.model.persistence.BuildSpecificationDao;
import com.zutubi.pulse.core.model.ResourceProperty;

import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

/**
 */
public class ViewBuildSpecificationAction extends ProjectActionSupport
{
    private BuildSpecification specification;
    private long id;
    private long selectedNode;
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
