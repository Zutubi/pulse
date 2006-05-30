/********************************************************************************
 @COPYRIGHT@
 ********************************************************************************/
package com.zutubi.pulse.web.project;

import com.zutubi.pulse.model.*;
import com.zutubi.pulse.xwork.interceptor.Cancelable;

/**
 */
public class CreateBuildStageAction extends BuildStageActionSupport implements Cancelable
{
    private long id;

    public long getId()
    {
        return id;
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

        return INPUT;
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

        if(getSpecification().getNodeByStageName(getName()) != null)
        {
            addFieldError("name", "A stage with name '" + getName() + "' already exists.");
        }
        
        lookupAgent();
    }

    public String execute()
    {
        addFieldsToStage();
        BuildSpecificationNode parent = getSpecification().getRoot();
        BuildSpecificationNode node = new BuildSpecificationNode(getStage());
        parent.addChild(node);
        getProjectManager().save(getProject());
        id = node.getId();
        return SUCCESS;
    }
}
