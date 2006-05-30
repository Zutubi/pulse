/********************************************************************************
  @COPYRIGHT@
 ********************************************************************************/
package com.zutubi.pulse.web.project;

import com.zutubi.pulse.model.BuildSpecification;
import com.zutubi.pulse.model.Project;
import com.zutubi.pulse.model.persistence.BuildSpecificationDao;

/**
 */
public class ViewBuildSpecificationAction extends ProjectActionSupport
{
    private BuildSpecification specification;
    private long id;
    private long selectedNode;

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
        return selectedNode != 0L;
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

        return SUCCESS;
    }
}
