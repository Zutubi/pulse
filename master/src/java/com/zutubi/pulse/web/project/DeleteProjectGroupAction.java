package com.zutubi.pulse.web.project;

import com.zutubi.pulse.model.ProjectGroup;
import com.zutubi.pulse.model.ProjectManager;
import com.zutubi.pulse.web.ActionSupport;

/**
 */
public class DeleteProjectGroupAction extends ActionSupport
{
    private long groupId;

    public void setGroupId(long groupId)
    {
        this.groupId = groupId;
    }

    public String execute() throws Exception
    {
        ProjectGroup group = projectManager.getProjectGroup(groupId);
        if(group != null)
        {
            projectManager.delete(group);
        }

        return SUCCESS;
    }
}
