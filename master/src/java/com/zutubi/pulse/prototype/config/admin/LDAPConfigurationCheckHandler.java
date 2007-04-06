package com.zutubi.pulse.prototype.config.admin;

import com.zutubi.validation.annotations.Required;
import com.zutubi.prototype.ConfigurationCheckHandler;
import com.zutubi.pulse.prototype.record.SymbolicName;

/**
 *
 *
 */
@SymbolicName("internal.ldapConfigutionCheckHandler")
public class LDAPConfigurationCheckHandler implements ConfigurationCheckHandler<LDAPConfiguration>
{
    @Required
    private String login;
    
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

    public void test(LDAPConfiguration configuration)
    {
        System.out.println(configuration.getBaseDn());
    }
}
