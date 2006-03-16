package com.cinnamonbob.web.admin.user;

import com.cinnamonbob.model.User;
import com.cinnamonbob.web.user.UserActionSupport;
import static com.cinnamonbob.model.GrantedAuthority.*;
import com.cinnamonbob.security.AcegiUtils;

/**
 *
 *
 */
public class EditUserAction extends UserActionSupport
{
    private boolean admin;

    public boolean isAdmin()
    {
        return admin;
    }

    public void setAdmin(boolean admin)
    {
        this.admin = admin;
    }

    public String doInput()
    {
        User user = getUser();
        admin = user.hasAuthority(ADMINISTRATOR);
        return INPUT;
    }

    public void validate()
    {
        // the currently logged in user can not remove admin permissions from themselves.
        // only another admin can do this, thereby ensuring that there is always at least
        // one admin in the system.
        User user = getUser();

        String loggedInUser = AcegiUtils.getLoggedInUser();
        if (user.getLogin().equals(loggedInUser) && (!admin))
        {
            addFieldError("admin", getText("admin.permission.self"));
        }
    }

    public String execute()
    {
        User persistentUser = getUser();
        if (admin)
        {
            if (!persistentUser.hasAuthority(ADMINISTRATOR))
            {
                persistentUser.add(ADMINISTRATOR);
                getUserManager().save(persistentUser);
            }
        }
        else
        {
            if (persistentUser.hasAuthority(ADMINISTRATOR))
            {
                persistentUser.remove(ADMINISTRATOR);
                getUserManager().save(persistentUser);
            }
        }
        return SUCCESS;
    }
}
