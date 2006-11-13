package com.zutubi.pulse.web.admin.user;

import com.zutubi.pulse.model.User;
import com.zutubi.pulse.web.user.UserActionSupport;

import java.util.Arrays;

/**
 *
 *
 */
public class EditUserAction extends UserActionSupport
{
    private String newLogin;
    private String newName;
    private boolean ldapAuthentication;
    private int startPage = 0;

    public String getNewLogin()
    {
        return newLogin;
    }

    public void setNewLogin(String newLogin)
    {
        this.newLogin = newLogin;
    }

    public String getNewName()
    {
        return newName;
    }

    public void setNewName(String newName)
    {
        this.newName = newName;
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
        newLogin = user.getLogin();
        newName = user.getName();
        ldapAuthentication = user.getLdapAuthentication();
        return INPUT;
    }

    public void validate()
    {
        User byLogin = getUserManager().getUser(newLogin);
        if(byLogin != null && byLogin.getId() != getUserId())
        {
            addFieldError("newLogin", getText("user.login.exists", Arrays.asList(newLogin)));
        }
    }

    public String execute()
    {
        User persistentUser = getUser();
        if(isAdminUser(persistentUser))
        {
            getConfigurationManager().getAppConfig().setAdminLogin(newLogin);
        }
        else
        {
            persistentUser.setLdapAuthentication(ldapAuthentication);
        }

        persistentUser.setLogin(newLogin);
        persistentUser.setName(newName);

        getUserManager().save(persistentUser);
        startPage = getUserStartPage(persistentUser);
        return SUCCESS;
    }
}
