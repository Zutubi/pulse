package com.cinnamonbob.web.user;

import java.util.List;

import com.cinnamonbob.model.User;

public class ViewUsersAction extends UserActionSupport
{
    private List<User> users;

    public List<User> getUsers()
    {
        return users;
    }
    
    public String execute()
    {
        // FIXME add a proper get all users query
        users = getUserManager().getUsersWithLoginLike("%");
        return SUCCESS;
    }
}
