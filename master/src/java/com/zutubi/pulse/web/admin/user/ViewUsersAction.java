package com.zutubi.pulse.web.admin.user;

import com.zutubi.pulse.UserLoginComparator;
import com.zutubi.pulse.model.User;
import com.zutubi.pulse.web.PagingSupport;
import com.zutubi.pulse.web.user.UserActionSupport;

import java.util.Collections;
import java.util.List;

public class ViewUsersAction extends UserActionSupport
{
    public static final int USERS_PER_PAGE = 20;

    private List<User> users;
    private PagingSupport pagingSupport = new PagingSupport(USERS_PER_PAGE);

    public List<User> getUsers()
    {
        return users;
    }

    public PagingSupport getPagingSupport()
    {
        return pagingSupport;
    }

    public void setStartPage(int page)
    {
        pagingSupport.setStartPage(page);
    }

    public String execute()
    {
        users = getUserManager().getAllUsers();
        Collections.sort(users, new UserLoginComparator());
        pagingSupport.setTotalItems(users.size());
        if(!pagingSupport.isStartPageValid())
        {
            setStartPage(0);
        }
        users = users.subList(pagingSupport.getStartOffset(), pagingSupport.getEndOffset());
        return SUCCESS;
    }
}
