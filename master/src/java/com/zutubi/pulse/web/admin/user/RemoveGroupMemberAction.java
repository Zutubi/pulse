package com.zutubi.pulse.web.admin.user;

import com.zutubi.pulse.model.Group;
import com.zutubi.pulse.model.User;

import java.util.Map;
import java.util.List;
import java.util.LinkedHashMap;

/**
 * An action to remove a member (user) from a group.
 */
public class RemoveGroupMemberAction extends GroupActionSupport
{
    private long userId;
    private int startPage;

    public long getUserId()
    {
        return userId;
    }

    public void setUserId(long userId)
    {
        this.userId = userId;
    }

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
        if(group == null)
        {
            addActionError("Unknown group [" + getGroupId() + "]");
            return ERROR;
        }

        User user = getUserManager().getUser(userId);
        if(user != null)
        {
            group.removeUser(user);
        }

        getUserManager().save(group);
        return SUCCESS;
    }
}
