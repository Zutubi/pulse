package com.cinnamonbob.web.user;

import java.util.List;

import com.cinnamonbob.user.User;

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
