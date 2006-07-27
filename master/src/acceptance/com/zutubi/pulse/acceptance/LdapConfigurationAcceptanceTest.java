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


    private void navigateToLdapConfiguration()
    {
        beginAt("/");
        clickLinkWithText("administration");
    }

    public void testEdit() throws Exception
    {
        navigateToLdapConfiguration();

        LdapConfigurationForm form = new LdapConfigurationForm(tester);

        assertAndClick("ldap.edit");
        form.assertFormPresent();

        form.saveFormElements("true", "ldap://dummy/", "dc=example,dc=com", "uid=admin", "password", "(uid={0})", "true");

        // The ldap config throws up a wait page, run around it :)
        navigateToLdapConfiguration();

        assertLdapTable("true", "ldap://dummy/", "dc=example,dc=com", "uid=admin", "(uid={0})", "true");

        assertAndClick("ldap.edit");
        form.assertFormElements("true", "ldap://dummy/", "dc=example,dc=com", "uid=admin", "password", "(uid={0})", "true");
        form.cancelFormElements("true", "ldap://dummy/", "dc=example,dc=com", "uid=admin", "password", "(uid={0})", "true");
    }

    public void testReset() throws Exception
    {
        navigateToLdapConfiguration();
        testEdit();

        assertAndClick("ldap.reset");
        assertLdapTable("false", "", "", "", "", "false");
    }

    public void testCancel() throws Exception
    {
        navigateToLdapConfiguration();

        assertAndClick("ldap.edit");
        LdapConfigurationForm form = new LdapConfigurationForm(tester);
        form.assertFormPresent();
        form.cancelFormElements("true", "oogie", "oogie", "oogie", "oogie", "boogie", "false");

        assertTextNotPresent("oogie");
    }

    public void testValidate() throws Exception
    {
        navigateToLdapConfiguration();

        assertAndClick("ldap.edit");
        LdapConfigurationForm form = new LdapConfigurationForm(tester);
        form.assertFormPresent();
        form.saveFormElements("true", "", "", "", "", "", "false");
        form.assertFormPresent();
        assertTextPresent("host is required");
        assertTextPresent("base dn is required");
        assertTextPresent("user filter is required");
    }

    private void assertLdapTable(String enabled, String host, String baseDn, String managerDn, String userFilter, String autoAdd)
    {
        assertTablePresent("ldap.config");
        // Can't assert directly on the table as it may or may not have a note row
        assertTextPresent(baseDn);
        assertTextPresent(managerDn);
        assertTextPresent(userFilter);
    }
}
