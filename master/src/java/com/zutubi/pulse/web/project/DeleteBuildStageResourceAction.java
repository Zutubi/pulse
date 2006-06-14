package com.zutubi.pulse.web.project;

import com.zutubi.pulse.model.ResourceRequirement;

import java.util.List;

/**
 */
public class DeleteBuildStageResourceAction extends BuildStageResourceActionSupport
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

        List<ResourceRequirement> resourceRequirements = node.getResourceRequirements();
        if(index - 1 >= resourceRequirements.size())
        {
            addActionError("Invalid resource index");
        }
    }

    public String execute() throws Exception
    {
        node.getResourceRequirements().remove(index - 1);
        getProjectManager().save(project);

        return SUCCESS;
    }
}
