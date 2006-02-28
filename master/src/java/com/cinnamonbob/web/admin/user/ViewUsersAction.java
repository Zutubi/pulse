package com.cinnamonbob.web.admin.user;

import java.util.List;

import com.cinnamonbob.model.User;
import com.cinnamonbob.web.user.UserActionSupport;

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
        return SUCCESS;
    }
}
