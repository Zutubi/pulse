package com.zutubi.pulse.prototype.config.admin;

import com.zutubi.config.annotations.SymbolicName;
import com.zutubi.prototype.ConfigurationCheckHandlerSupport;
import com.zutubi.pulse.security.ldap.LdapManager;
import com.zutubi.validation.annotations.Required;

/**
 *
 *
 */
@SymbolicName("internal.ldapConfigutionCheckHandler")
public class LDAPConfigurationCheckHandler extends ConfigurationCheckHandlerSupport<LDAPConfiguration>
{
    @Required
    private String login;
    private String password;

    private LdapManager ldapManager;

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
        ldapManager.testAuthenticate(configuration, login, password);
    }

    public void setLdapManager(LdapManager ldapManager)
    {
        this.ldapManager = ldapManager;
    }
}
