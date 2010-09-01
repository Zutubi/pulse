package com.zutubi.pulse.master.tove.config.admin;

import com.zutubi.pulse.master.security.ldap.LdapManager;
import com.zutubi.tove.annotations.SymbolicName;
import com.zutubi.tove.annotations.Wire;
import com.zutubi.tove.annotations.Form;
import com.zutubi.tove.config.api.AbstractConfigurationCheckHandler;
import com.zutubi.validation.annotations.Required;

/**
 * Configuration checker for LDAP Configurations.  This checker
 * tests whether a set of credentials can be authenticated via
 * LDAP using the configuration being tested.
 */
@Form(fieldOrder = {"login", "password"})
@SymbolicName("zutubi.ldapConfigutionCheckHandler")
@Wire
public class LDAPConfigurationCheckHandler extends AbstractConfigurationCheckHandler<LDAPConfiguration>
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
        if (configuration.isEnabled())
        {
            ldapManager.testAuthenticate(configuration, login, password);
        }
    }

    public void setLdapManager(LdapManager ldapManager)
    {
        this.ldapManager = ldapManager;
    }
}
