package com.zutubi.pulse.web.project;

/**
 */
public class AddBuildStageResourceAction extends BuildStageResourceActionSupport
{
    public String doInput() throws Exception
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

        return INPUT;
    }

    public String execute() throws Exception
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

        addFieldsToResource();
        node.getResourceRequirements().add(resourceRequirement);
        getProjectManager().save(project);
        return SUCCESS;
    }
}
