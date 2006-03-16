package com.cinnamonbob.web.admin.user;

import com.cinnamonbob.UserLoginComparator;
import com.cinnamonbob.model.User;
import com.cinnamonbob.web.user.UserActionSupport;

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
