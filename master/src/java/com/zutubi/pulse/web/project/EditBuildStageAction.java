/********************************************************************************
 @COPYRIGHT@
 ********************************************************************************/
package com.zutubi.pulse.web.project;

import com.zutubi.pulse.model.*;
import com.zutubi.pulse.xwork.interceptor.Cancelable;

/**
 */
public class EditBuildStageAction extends BuildStageActionSupport implements Cancelable
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

    public String doInput()
    {
        lookupProject(getProjectId());
        if(hasErrors())
        {
            return ERROR;
        }

        lookupSpec();
        if(hasErrors())
        {
            return ERROR;
        }

        lookupNode();
        if(hasErrors())
        {
            return ERROR;
        }

        stage = node.getStage();
        getAgentFromStage();
        return INPUT;
    }

    private void lookupNode()
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
        if(hasErrors())
        {
            return;
        }

        lookupAgent();
    }

    public String execute()
    {
        addFieldsToStage();
        node.setStage(stage);
        getProjectManager().save(getProject());
        return SUCCESS;
    }
}
