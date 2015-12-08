package com.zutubi.pulse.master.xwork.actions;

/**
 * This login action is to provide mapping support between how Acegi expects the
 * data and how webwork likes to present it.
 */
public class LoginAction extends ActionSupport
{
    private boolean authenticationError = false;
    private String username;
    private boolean rememberMe;

    public String getUsername()
    {
        return username;
    }

    public void setUsername(String username)
    {
        this.username = username;
    }

    public boolean isRememberMe()
    {
        return rememberMe;
    }

    public void setRememberMe(boolean rememberMe)
    {
        this.rememberMe = rememberMe;
    }

    public void setError(boolean error)
    {
        this.authenticationError = error;
    }

    @Override
    public String doInput() throws Exception
    {
        if (authenticationError)
        {
            addActionError(getText("login.badcredentials"));
        }


        return INPUT;
    }

    @Override
    public String execute() throws Exception
    {
        return SUCCESS;
    }
}
