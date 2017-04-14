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

package com.zutubi.pulse.acceptance;

import com.zutubi.pulse.acceptance.rpc.RpcClient;
import com.zutubi.pulse.master.tove.config.admin.LDAPConfiguration;
import junit.extensions.TestSetup;
import junit.framework.Test;

import static java.util.Arrays.asList;
import java.util.Hashtable;
import java.util.Vector;

public class EnableLdapTestSetup extends TestSetup
{
    private static final String LDAP_CONFIG_PATH = "settings/ldap";

    private RpcClient rpcClient;

    public EnableLdapTestSetup(Test test)
    {
        super(test);
    }

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();

        rpcClient = new RpcClient();
        rpcClient.loginAsAdmin();

        Hashtable<String, Object> ldapConfig = rpcClient.RemoteApi.createDefaultConfig(LDAPConfiguration.class);
        ldapConfig.put("enabled", Boolean.TRUE);
        ldapConfig.put("ldapUrl", "ldap://localhost:10389/");
        ldapConfig.put("baseDn", "dc=zutubi,dc=com");
        ldapConfig.put("managerDn", "uid=admin,ou=system");
        ldapConfig.put("managerPassword", "secret");
        ldapConfig.put("userBaseDn", "ou=users");
        ldapConfig.put("userFilter", "(uid=${login})");
        ldapConfig.put("groupBaseDns", new Vector<String>(asList("ou=groups")));

        rpcClient.RemoteApi.saveConfig(LDAP_CONFIG_PATH, ldapConfig, false);
    }

    @Override
    protected void tearDown() throws Exception
    {
        Hashtable<String, Object> ldapConfig = rpcClient.RemoteApi.getConfig(LDAP_CONFIG_PATH);
        ldapConfig.put("enabled", Boolean.FALSE);

        rpcClient.RemoteApi.saveConfig(LDAP_CONFIG_PATH, ldapConfig, false);

        super.tearDown();
    }
}
