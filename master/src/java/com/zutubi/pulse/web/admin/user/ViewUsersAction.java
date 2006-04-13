/********************************************************************************
 @COPYRIGHT@
 ********************************************************************************/
package com.zutubi.pulse.web.admin.user;

import com.zutubi.pulse.UserLoginComparator;
import com.zutubi.pulse.model.User;
import com.zutubi.pulse.web.user.UserActionSupport;

import java.util.Collections;
import java.util.List;

public class ViewUsersAction extends UserActionSupport
{
    private List<User> users;

    public List<User> getUsers()
    {
        return users;
    }

    public String execute()
    {
        users = getUserManager().getAllUsers();
        Collections.sort(users, new UserLoginComparator());
        return SUCCESS;
    }
}
