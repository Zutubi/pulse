package com.zutubi.pulse.web.project;

import com.zutubi.pulse.model.*;
import com.zutubi.pulse.xwork.interceptor.Cancelable;

/**
 */
public class DeleteBuildStageAction extends BuildStageActionSupport implements Cancelable
{
    private long id;
    BuildSpecificationNode node;

    public long getId()
    {
        return id;
    }

    public void setId(long id)
    {
        this.id = id;
    }

    protected void lookupNode()
    {
        node = getSpecification().getNode(id);
        if(node == null)
        {
            addActionError("Unknown stage [" + id + "]");
        }
    }

    public void validate()
    {
        lookupProject(getProjectId());
        if (hasErrors())
        {
            return;
        }

        lookupSpec();
        if(hasErrors())
        {
            return;
        }

        lookupNode();
    }

    public String execute()
    {
        getSpecification().getRoot().removeChild(id);
        getProjectManager().save(getProject());
        return SUCCESS;
    }
}
