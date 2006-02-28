package com.cinnamonbob.web.admin.user;

import com.cinnamonbob.web.ActionSupport;
import com.cinnamonbob.model.User;
import com.cinnamonbob.model.UserManager;

import java.util.Arrays;

/**
 * <class-comment/>
 */
public class CreateUserAction extends ActionSupport
{
    private User user = new User();
    private String confirm;

    private UserManager userManager;

    public Object getUser()
    {
        return user;
    }

    public String getConfirm()
    {
        return confirm;
    }

    public void setConfirm(String confirm)
    {
        this.confirm = confirm;
    }

    public void validate()
    {
        if (hasErrors())
        {
            // do not attempt to validate unless all other validation rules have
            // completed successfully.
            return;
        }
        // check the password confirmation.
        if (!confirm.equals(user.getPassword()))
        {
            addFieldError("confirm", getText("user.confirm.mismatch"));
        }

        // check that the user does not already exist.
        if (userManager.getUser(user.getLogin()) != null)
        {
            // login name already in use.
            addFieldError("user.login", getText("user.login.exists", Arrays.asList(user.getLogin())));
        }
    }

    public String execute() throws Exception
    {
        userManager.save(user);
        doReset();
        return SUCCESS;
    }

    public void doReset()
    {
        // reset the user details.
        user = new User();
    }

    public void setUserManager(UserManager userManager)
    {
        this.userManager = userManager;
    }
}
