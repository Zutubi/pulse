package com.zutubi.pulse.web.admin.user;

import com.zutubi.pulse.model.Group;

/**
 * Action to delete a group.
 */
public class DeleteGroupAction extends GroupActionSupport
{
    private int startPage;

    public int getStartPage()
    {
        return startPage;
    }

    public void setStartPage(int startPage)
    {
        this.startPage = startPage;
    }

    public String execute() throws Exception
    {
        Group group = getGroup();
        if(group != null)
        {
            getUserManager().delete(group, getProjectManager());
        }

        return SUCCESS;
    }
}
