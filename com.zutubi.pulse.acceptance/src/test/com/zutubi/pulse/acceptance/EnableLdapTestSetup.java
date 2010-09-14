package com.zutubi.pulse.acceptance;

import junit.extensions.TestSetup;
import junit.framework.Test;

import java.util.Hashtable;
import java.util.Vector;
import static java.util.Arrays.asList;

import com.zutubi.pulse.master.tove.config.admin.LDAPConfiguration;

public class EnableLdapTestSetup extends TestSetup
{
    private static final String LDAP_CONFIG_PATH = "settings/ldap";

    private XmlRpcHelper xmlRpcHelper;

    public EnableLdapTestSetup(Test test)
    {
        super(test);
    }

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();

        xmlRpcHelper = new XmlRpcHelper();
        xmlRpcHelper.loginAsAdmin();

        Hashtable<String, Object> ldapConfig = xmlRpcHelper.createDefaultConfig(LDAPConfiguration.class);
        ldapConfig.put("enabled", Boolean.TRUE);
        ldapConfig.put("ldapUrl", "ldap://localhost:10389/");
        ldapConfig.put("baseDn", "dc=ldap-test,dc=zutubi,dc=com");
        ldapConfig.put("managerDn", "uid=admin,ou=system");
        ldapConfig.put("managerPassword", "secret");
        ldapConfig.put("userFilter", "(uid=${login})");
        ldapConfig.put("userBaseDn", "ou=Users");
        ldapConfig.put("groupBaseDns", new Vector<String>(asList("ou=Groups")));

        xmlRpcHelper.saveConfig(LDAP_CONFIG_PATH, ldapConfig, false);
    }

    @Override
    protected void tearDown() throws Exception
    {
        Hashtable<String, Object> ldapConfig = xmlRpcHelper.getConfig(LDAP_CONFIG_PATH);
        ldapConfig.put("enabled", Boolean.FALSE);

        xmlRpcHelper.saveConfig(LDAP_CONFIG_PATH, ldapConfig, false);

        super.tearDown();
    }
}
