package com.cinnamonbob.web.user;

import com.cinnamonbob.model.User;

/**
 * 
 *
 */
public class ViewUserAction extends UserActionSupport
{
    private long id;
    private String login;

    private User user;

    public long getId()
    {
        return id;
    }

    public void setId(long id)
    {
        this.id = id;
    }

    public String getLogin()
    {
        return login;
    }

    public void setLogin(String login)
    {
        this.login = login;
    }

    public User getUser()
    {
        return user;
    }

    public void validate()
    {
        if (id == 0 && (login == null || login.length() == 0))
        {
            addActionError("Please specify a user to view.");
        }
    }

    public String execute()
    {
        if (id != 0)
        {
            user = getUserManager().getUser(id);
        }
        else
        {
            user = getUserManager().getUser(login);
        }
        return SUCCESS;
    }
}
