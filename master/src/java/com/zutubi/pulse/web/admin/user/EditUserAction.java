package com.zutubi.pulse.web.admin.user;

import static com.zutubi.pulse.model.GrantedAuthority.ADMINISTRATOR;
import com.zutubi.pulse.model.User;
import com.zutubi.pulse.security.AcegiUtils;
import com.zutubi.pulse.web.user.UserActionSupport;

/**
 *
 *
 */
public class EditUserAction extends UserActionSupport
{
    private boolean admin;
    private boolean ldapAuthentication;

    public boolean isAdmin()
    {
        return admin;
    }

    public void setAdmin(boolean admin)
    {
        this.admin = admin;
    }

    public boolean isLdapAuthentication()
    {
        return ldapAuthentication;
    }

    public void setLdapAuthentication(boolean ldapAuthentication)
    {
        this.ldapAuthentication = ldapAuthentication;
    }

    public String doInput()
    {
        User user = getUser();
        admin = user.hasAuthority(ADMINISTRATOR);
        ldapAuthentication = user.getLdapAuthentication();
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
            }
        }
        else
        {
            if (persistentUser.hasAuthority(ADMINISTRATOR))
            {
                persistentUser.remove(ADMINISTRATOR);
            }
        }

        persistentUser.setLdapAuthentication(ldapAuthentication);
        getUserManager().save(persistentUser);
        return SUCCESS;
    }
}
