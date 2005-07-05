package com.cinnamonbob.web;

import com.opensymphony.xwork.ActionSupport;
import com.cinnamonbob.model.User;
import com.cinnamonbob.model.UserManager;

/**
 * 
 *
 */
public class CreateUserAction extends ActionSupport
{
    private User user = new User();

    private UserManager userManager;

    public void setUserManager(UserManager userManager)
    {
        this.userManager = userManager;
    }
    
    public User getUser()
    {
        return user;
    }

    public void validate()
    {
        if (hasErrors())
        {
            // do not attempt to validate unless all other validation rules have 
            // completed successfully.
            return;
        }

        if (userManager.getUser(user.getLogin()) != null)
        {
            // login name already in use.
            addFieldError("user.login", "Login name " + user.getLogin() + " is already being used.");
        }
    }
    
    public String execute()
    {        
        // store user.
        userManager.createNewUser(user);
        
        return SUCCESS;
    }

    public String doDefault()
    {
        // setup any default data.
        return SUCCESS;
    }

}
