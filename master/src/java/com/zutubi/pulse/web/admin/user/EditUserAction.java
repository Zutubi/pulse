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
    private int startPage = 0;

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

    public int getStartPage()
    {
        return startPage;
    }

    public String doInput()
    {
        User user = getUser();
        startPage = getUserStartPage(user);
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
        String login = user.getLogin();
        if (login.equals(loggedInUser) && (!admin))
        {
            addFieldError("admin", getText("admin.permission.self"));
        }

        if(isAdminUser(user))
        {
            addActionError(getText("user.edit.admin"));
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
        startPage = getUserStartPage(persistentUser);
        return SUCCESS;
    }
}
