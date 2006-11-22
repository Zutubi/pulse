package com.zutubi.pulse.web.project;

import com.zutubi.pulse.model.BuildSpecification;
import com.zutubi.pulse.model.BuildSpecificationNode;
import com.zutubi.pulse.model.Project;

/**
 */
public class DeletePostBuildAction extends ProjectActionSupport
{
    private long id;
    private long specId;
    private long nodeId;

    public long getId()
    {
        return id;
    }

    public void setId(long id)
    {
        this.id = id;
    }

    public long getSpecId()
    {
        return specId;
    }

    public void setSpecId(long specId)
    {
        this.specId = specId;
    }

    public long getNodeId()
    {
        return nodeId;
    }

    public void setNodeId(long nodeId)
    {
        this.nodeId = nodeId;
    }

    public String execute() throws Exception
    {
        Project project = getProjectManager().getProject(projectId);
        if(project == null)
        {
            addActionError("Unknown project [" + projectId + "]");
            return ERROR;
        }

        if(specId > 0)
        {
            BuildSpecification spec = project.getBuildSpecification(specId);
            if(spec == null)
            {
                addActionError("Unknown build specification [" + specId + "]");
                return ERROR;
            }

            BuildSpecificationNode node = spec.getNode(nodeId);
            if(node == null)
            {
                addActionError("Unknown stage [" + nodeId + "]");
                return ERROR;
            }

            node.removePostAction(id);
        }
        else
        {
            project.removePostBuildAction(id);
        }

        getProjectManager().save(project);
        return SUCCESS;
    }
}
