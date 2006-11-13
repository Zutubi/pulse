package com.zutubi.pulse.acceptance;

import com.zutubi.pulse.acceptance.forms.LdapConfigurationForm;

/**
 * <class-comment/>
 */
public class LdapConfigurationAcceptanceTest extends BaseAcceptanceTestCase
{
    public LdapConfigurationAcceptanceTest()
    {
    }

    public LdapConfigurationAcceptanceTest(String name)
    {
        super(name);
    }

    protected void setUp() throws Exception
    {
        super.setUp();
        loginAsAdmin();
    }

    protected void tearDown() throws Exception
    {
        super.tearDown();
    }

    public void testEdit() throws Exception
    {
        navigateToLdapConfiguration();

        LdapConfigurationForm form = new LdapConfigurationForm(tester);

        assertAndClick(Navigation.Administration.LINK_EDIT_LDAP);
        form.assertFormPresent();

        form.saveFormElements("true", "ldap://dummy/", "dc=example,dc=com", "uid=admin", "password", "(uid={0})", "true", "mail");

        // The ldap config throws up a wait page, run around it :)
        navigateToLdapConfiguration();

        assertLdapTable("true", "ldap://dummy/", "dc=example,dc=com", "uid=admin", "(uid={0})", "true", "mail");

        assertAndClick(Navigation.Administration.LINK_EDIT_LDAP);
        form.assertFormElements("true", "ldap://dummy/", "dc=example,dc=com", "uid=admin", "password", "(uid={0})", "true", "mail");
        form.cancelFormElements("true", "ldap://dummy/", "dc=example,dc=com", "uid=admin", "password", "(uid={0})", "true", "mail");
    }

    public void testReset() throws Exception
    {
        navigateToLdapConfiguration();
        testEdit();

        assertAndClick(Navigation.Administration.LINK_RESET_LDAP);
        assertLdapTable("false", "", "", "", "", "false", "");
    }

    public void testCancel() throws Exception
    {
        navigateToLdapConfiguration();

        assertAndClick(Navigation.Administration.LINK_EDIT_LDAP);
        LdapConfigurationForm form = new LdapConfigurationForm(tester);
        form.assertFormPresent();
        form.cancelFormElements("true", "oogie", "oogie", "oogie", "oogie", "boogie", "false", "boogie");

        assertTextNotPresent("oogie");
    }

    public void testValidate() throws Exception
    {
        navigateToLdapConfiguration();

        assertAndClick(Navigation.Administration.LINK_EDIT_LDAP);
        LdapConfigurationForm form = new LdapConfigurationForm(tester);
        form.assertFormPresent();
        form.saveFormElements("true", "", "", "", "", "", "false", "");
        form.assertFormPresent();
        assertTextPresent("host is required");
        assertTextPresent("base dn is required");
        assertTextPresent("user filter is required");
    }

    private void assertLdapTable(String enabled, String host, String baseDn, String managerDn, String userFilter, String autoAdd, String emailAttribute)
    {
        assertTablePresent("ldap.config");
        // Can't assert directly on the table as it may or may not have a note row
        assertTextPresent(baseDn);
        assertTextPresent(managerDn);
        assertTextPresent(userFilter);
        assertTextPresent(emailAttribute);
    }
}
