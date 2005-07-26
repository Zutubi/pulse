package com.cinnamonbob.web.user;

import com.cinnamonbob.model.UserManager;
import com.cinnamonbob.web.BaseActionSupport;
import com.opensymphony.xwork.ActionSupport;

import java.util.Collections;
import java.util.List;

/**
 * Search for users with the login like .... This action supports wildcard searching
 * using the '*' character.
 * 
 *
 */
public class FindUsersAction extends BaseActionSupport
{
    private UserManager userManager;
    private String login;
    private List results = Collections.EMPTY_LIST;

    public void setUserManager(UserManager userManager)
    {
        this.userManager = userManager;
    }

    public void setLogin(String login)
    {
        this.login = login;
    }

    public String getLogin()
    {
        return login;
    }
    
    public List getResults()
    {
        return results;
    }

    public String execute()
    {
        if (login == null)
            login = "";

        // for now, make use of the databases wildcard matching
        results = userManager.getUsersWithLoginLike(login.replace('*', '%'));

        return SUCCESS;
    }
}
