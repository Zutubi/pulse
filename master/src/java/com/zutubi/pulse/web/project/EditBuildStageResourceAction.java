package com.zutubi.pulse.web.project;

import com.zutubi.pulse.model.ResourceRequirement;

import java.util.List;

/**
 */
public class EditBuildStageResourceAction extends BuildStageResourceActionSupport
{
    private int index;

    public int getIndex()
    {
        return index;
    }

    public void setIndex(int index)
    {
        this.index = index;
    }

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

        List<ResourceRequirement> resourceRequirements = validateIndex();
        if(hasErrors())
        {
            return ERROR;
        }

        resourceRequirement = resourceRequirements.get(index - 1);

        return INPUT;
    }

    private List<ResourceRequirement> validateIndex()
    {
        List<ResourceRequirement> resourceRequirements = node.getResourceRequirements();
        if(index - 1 >= resourceRequirements.size())
        {
            addActionError("Invalid resource index");
        }
        return resourceRequirements;
    }

    public void validate()
    {
        lookupProject(getProjectId());
        if(hasErrors())
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

        validateIndex();
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

        ResourceRequirement persistentResource = node.getResourceRequirements().get(index - 1);
        persistentResource.setResource(resourceRequirement.getResource());
        persistentResource.setVersion(resourceRequirement.getVersion());

        getProjectManager().save(project);

        return SUCCESS;
    }
}
