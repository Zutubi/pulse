/* Copyright 2017 Zutubi Pty Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
