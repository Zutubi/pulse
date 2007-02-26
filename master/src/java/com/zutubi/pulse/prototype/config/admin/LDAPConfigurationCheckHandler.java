package com.zutubi.pulse.prototype.config.admin;

import com.zutubi.validation.annotations.Required;
import com.zutubi.prototype.ConfigurationCheckHandler;

/**
 *
 *
 */
public class LDAPConfigurationCheckHandler implements ConfigurationCheckHandler<LDAPConfiguration>
{
    private String login;
    private String password;

    @Required()
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

    public void test(LDAPConfiguration configuration)
    {

    }
}
