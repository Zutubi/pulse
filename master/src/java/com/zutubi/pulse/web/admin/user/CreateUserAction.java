package com.zutubi.pulse.web.admin.user;

import com.zutubi.pulse.model.User;
import com.zutubi.pulse.web.user.UserActionSupport;
import com.zutubi.pulse.license.Licensed;

import java.util.Arrays;

/**
 * <class-comment/>
 * 
 */
@Licensed("canAddUser")
public class CreateUserAction extends UserActionSupport
{
    private User newUser = new User();
    private String confirm;
    private boolean ldapAuthentication = false;
    private boolean grantAdminPermissions;
    private int startPage = 0;

    public User getNewUser()
    {
        return newUser;
    }

    public String getConfirm()
    {
        return confirm;
    }

    public void setConfirm(String confirm)
    {
        this.confirm = confirm;
    }

    public boolean isLdapAuthentication()
    {
        return ldapAuthentication;
    }

    public void setLdapAuthentication(boolean ldapAuthentication)
    {
        this.ldapAuthentication = ldapAuthentication;
    }

    public boolean isAdmin()
    {
        return grantAdminPermissions;
    }

    public void setAdmin(boolean admin)
    {
        this.grantAdminPermissions = admin;
    }

    public int getStartPage()
    {
        return startPage;
    }

    public void validate()
    {
        if (hasErrors())
        {
            // do not attempt to validate unless all other validation rules have
            // completed successfully.
            return;
        }

        if (!ldapAuthentication)
        {
            // check the password confirmation.
            if (!confirm.equals(newUser.getPassword()))
            {
                addFieldError("confirm", getText("password.confirm.mismatch"));
            }
        }

        // check that the user does not already exist.
        if (getUserManager().getUser(newUser.getLogin()) != null)
        {
            // login name already in use.
            addFieldError("newUser.login", getText("user.login.exists", Arrays.asList(newUser.getLogin())));
        }
    }

    public String execute() throws Exception
    {
        getUserManager().addUser(newUser, grantAdminPermissions, ldapAuthentication);
        startPage = getUserStartPage(newUser);
        doReset();
        return SUCCESS;
    }

    public void doReset()
    {
        // reset the user details.
        newUser = new User();
    }
}
