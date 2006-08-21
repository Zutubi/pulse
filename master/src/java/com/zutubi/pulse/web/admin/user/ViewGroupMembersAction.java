package com.zutubi.pulse.web.admin.user;

import com.zutubi.pulse.model.Group;
import com.zutubi.pulse.model.User;
import com.zutubi.pulse.web.PagingSupport;
import com.zutubi.pulse.UserLoginComparator;

import java.util.List;
import java.util.Collections;
import java.util.ArrayList;

/**
 */
public class ViewGroupMembersAction extends GroupActionSupport
{
    public static int USERS_PER_PAGE = 20;

    private List<User> users;
    private PagingSupport pagingSupport = new PagingSupport(USERS_PER_PAGE);

    public PagingSupport getPagingSupport()
    {
        return pagingSupport;
    }

    public int getStartPage()
    {
        return pagingSupport.getStartPage();
    }

    public void setStartPage(int page)
    {
        pagingSupport.setStartPage(page);
    }

    public List<User> getUsers()
    {
        return users;
    }

    public String execute() throws Exception
    {
        Group group = getGroup();
        if(group == null)
        {
            addActionError("Unknown group [" + getGroupId() + "]");
            return ERROR;
        }

        users = new ArrayList<User>(group.getUsers());
        Collections.sort(users, new UserLoginComparator());
        pagingSupport.setTotalItems(users.size());
        pagingSupport.clampStartPage();
        users = users.subList(pagingSupport.getStartOffset(), pagingSupport.getEndOffset());
        setStartPage(getGroupStartPage(group));
        
        return SUCCESS;
    }
}
