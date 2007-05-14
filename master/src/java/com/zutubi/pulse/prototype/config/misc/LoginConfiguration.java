package com.zutubi.pulse.prototype.config.misc;

import com.zutubi.config.annotations.Form;
import com.zutubi.config.annotations.Password;
import com.zutubi.config.annotations.SymbolicName;

/**
 */
@Form(fieldOrder = { "login", "password" })
@SymbolicName("internal.transient.login")
public class LoginConfiguration
{
    private String login;
    @Password
    private String password;

    public String getLogin()
    {
        return login;
    }

    public void setLogin(String login)
    {
        this.login = login;
    }

    public String getPassword()
    {
        return password;
    }

    public void setPassword(String password)
    {
        this.password = password;
    }
}
