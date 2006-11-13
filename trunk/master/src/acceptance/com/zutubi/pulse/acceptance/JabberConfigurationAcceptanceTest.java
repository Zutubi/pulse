package com.zutubi.pulse.acceptance;

import com.zutubi.pulse.acceptance.forms.JabberConfigurationForm;

/**
 * <class-comment/>
 */
public class JabberConfigurationAcceptanceTest extends BaseAcceptanceTestCase
{
    public JabberConfigurationAcceptanceTest()
    {
    }

    public JabberConfigurationAcceptanceTest(String name)
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
        navigateToJabberConfiguration();

        JabberConfigurationForm form = new JabberConfigurationForm(tester);

        assertAndClick(Navigation.Administration.LINK_EDIT_JABBER);
        form.assertFormPresent();

        form.saveFormElements("testhost", "10101", "testuser", "testpassword");

        // The jabber config throws up a wait page, run around it :)
        navigateToJabberConfiguration();

        assertJabberTable("testhost", "10101", "testuser");

        assertAndClick("jabber.edit");
        form.assertFormElements("testhost", "10101", "testuser", "testpassword");
        form.cancelFormElements("testhost", "10101", "testuser", "testpassword");
    }

    public void testReset() throws Exception
    {
        navigateToJabberConfiguration();
        testEdit();

        assertAndClick(Navigation.Administration.LINK_RESET_JABBER);
        assertJabberTable("", "5222", "");
    }

    public void testCancel() throws Exception
    {
        navigateToJabberConfiguration();

        assertAndClick(Navigation.Administration.LINK_EDIT_JABBER);
        JabberConfigurationForm form = new JabberConfigurationForm(tester);
        form.assertFormPresent();
        form.cancelFormElements("boohoo", "1", "hoo", "hoo");

        assertTextNotPresent("boohoo");
    }

    public void testValidate() throws Exception
    {
        navigateToJabberConfiguration();

        assertAndClick(Navigation.Administration.LINK_EDIT_JABBER);
        JabberConfigurationForm form = new JabberConfigurationForm(tester);
        form.assertFormPresent();
        form.saveFormElements("", "-1", "", "");
        form.assertFormPresent();
        assertTextPresent("jabber server is required");
        assertTextPresent("port must be a positive integer");
        assertTextPresent("username is required");
    }

    private void assertJabberTable(String host, String port, String username)
    {
        assertTablePresent("jabber.config");
        // Can't assert directly on the table as it may or may not have a note row
        assertTextPresent(host);
        assertTextPresent(port);
        assertTextPresent(username);
    }
}
