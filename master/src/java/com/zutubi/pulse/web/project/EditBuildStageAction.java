package com.zutubi.pulse.web.project;

import com.zutubi.pulse.model.*;
import com.zutubi.pulse.xwork.interceptor.Cancelable;

/**
 */
public class EditBuildStageAction extends BuildStageActionSupport implements Cancelable
{

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
        getFieldsFromStage();
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

        lookupNode();
        if(hasErrors())
        {
            return;
        }

        BuildSpecificationNode withName = getSpecification().getNodeByStageName(name);
        if(withName != null && withName.getId() != node.getId())
        {
            addFieldError("name", "A stage with name '" + name + "' already exists.");
        }

        lookupAgent();
    }

    public String execute()
    {
        node.setStage(stage);
        addFieldsToStage();
        getProjectManager().save(getProject());
        return SUCCESS;
    }
}
