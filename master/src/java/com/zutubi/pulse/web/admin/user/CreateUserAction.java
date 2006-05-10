/********************************************************************************
 @COPYRIGHT@
 ********************************************************************************/
package com.zutubi.pulse.web.admin.user;

import com.zutubi.pulse.model.User;
import com.zutubi.pulse.model.GrantedAuthority;
import com.zutubi.pulse.model.UserManager;
import com.zutubi.pulse.web.user.UserActionSupport;
import com.zutubi.pulse.web.DefaultAction;

import java.util.Arrays;

/**
 * <class-comment/>
 */
public class CreateUserAction extends UserActionSupport
{
    private User newUser = new User();
    private String confirm;
    private boolean grantAdminPermissions;

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

    public boolean isAdmin()
    {
        return grantAdminPermissions;
    }

    public void setAdmin(boolean admin)
    {
        this.grantAdminPermissions = admin;
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
        if (!confirm.equals(newUser.getPassword()))
        {
            addFieldError("confirm", getText("password.confirm.mismatch"));
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
        // ensure that the user has the correct authorities to login.
        newUser.add(GrantedAuthority.USER);
        if (grantAdminPermissions)
        {
            newUser.add(GrantedAuthority.ADMINISTRATOR);
        }
        newUser.setEnabled(true);
        newUser.setDefaultAction(DefaultAction.WELCOME_ACTION);
        UserManager userManager = getUserManager();
        userManager.save(newUser);
        // can only update the password on a persistent user since the password salt relies
        // upon the users id.
        userManager.setPassword(newUser, newUser.getPassword());
        userManager.save(newUser);
        
        doReset();
        return SUCCESS;
    }

    public void doReset()
    {
        // reset the user details.
        newUser = new User();
    }
}
