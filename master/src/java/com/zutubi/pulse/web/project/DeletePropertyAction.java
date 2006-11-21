package com.zutubi.pulse.web.project;

import com.zutubi.pulse.web.admin.ResourceActionSupport;
import com.zutubi.pulse.core.model.ResourceProperty;
import com.zutubi.pulse.model.BuildSpecification;
import com.zutubi.pulse.model.Project;

/**
 */
public class DeletePropertyAction extends ProjectActionSupport
{
    private long specId;
    private long id;

    public long getSpecId()
    {
        return specId;
    }

    public void setSpecId(long specId)
    {
        this.specId = specId;
    }

    public long getId()
    {
        return id;
    }

    public void setId(long id)
    {
        this.id = id;
    }

    public String execute()
    {
        Project p = lookupProject(projectId);
        if(p != null)
        {
            BuildSpecification spec = p.getBuildSpecification(specId);
            if(spec != null)
            {
                spec.removeProperty(id);
                projectManager.save(p);
            }
        }

        return SUCCESS;
    }
}
