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
    private boolean ldapAuthentication;
    private int startPage = 0;

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
        ldapAuthentication = user.getLdapAuthentication();
        return INPUT;
    }

    public void validate()
    {
        User user = getUser();
        if(isAdminUser(user))
        {
            addActionError(getText("user.edit.admin"));
        }
    }

    public String execute()
    {
        User persistentUser = getUser();
        persistentUser.setLdapAuthentication(ldapAuthentication);
        getUserManager().save(persistentUser);
        startPage = getUserStartPage(persistentUser);
        return SUCCESS;
    }
}
