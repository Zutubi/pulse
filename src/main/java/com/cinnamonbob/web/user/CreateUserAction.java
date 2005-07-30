package com.cinnamonbob.web.user;

import com.cinnamonbob.model.User;
import com.cinnamonbob.model.UserManager;
import com.cinnamonbob.web.ActionSupport;

/**
 * 
 *
 */
public class CreateUserAction extends UserActionSupport
{
    private User user = new User();

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

        if (getUserManager().getUser(user.getLogin()) != null)
        {
            // login name already in use.
            addFieldError("user.login", "Login name " + user.getLogin() + " is already being used.");
        }
    }

    public String execute()
    {
        // store user.
        getUserManager().createNewUser(user);

        return SUCCESS;
    }

    public String doDefault()
    {
        // setup any default data.
        return SUCCESS;
    }

}
