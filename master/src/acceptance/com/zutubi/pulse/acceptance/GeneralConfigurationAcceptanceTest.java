package com.zutubi.pulse.acceptance;

import com.zutubi.pulse.acceptance.forms.GeneralConfigurationForm;

/**
 * <class-comment/>
 */
public class GeneralConfigurationAcceptanceTest extends BaseAcceptanceTest
{
    public GeneralConfigurationAcceptanceTest()
    {
    }

    public GeneralConfigurationAcceptanceTest(String name)
    {
        super(name);
    }

    protected void setUp() throws Exception
    {
        super.setUp();

        login("admin", "admin");
    }

    protected void tearDown() throws Exception
    {
        super.tearDown();
    }

    public void testReset() throws Exception
    {
        navigateToGeneralConfiguration();

        clickLinkWithText("reset");
        assertTextPresent("localhost:" + port);
        assertTextPresent("http://confluence.zutubi.com/display/Pulse");
    }

    public void testEdit() throws Exception
    {
        navigateToGeneralConfiguration();
        // ensure that we are not starting with the email address we using for this test.
        assertTextNotPresent("saved.host.net");
        assertTextNotPresent("saved.help.url");

        clickLink("general.edit");

        GeneralConfigurationForm form = new GeneralConfigurationForm(tester);

        form.assertFormPresent();
        form.saveFormElements("saved.host.net", "saved.help.url", "true", "true");

        form.assertFormNotPresent();

        assertTextPresent("saved.host.net");
        assertTextPresent("saved.help.url");

        clickLink("general.edit");
        form.assertFormPresent();
        form.assertFormElements("saved.host.net", "saved.help.url", "true", "true");
        form.saveFormElements("saved.host.net", "saved.help.url", "true", "false");
    }

    public void testCancel() throws Exception
    {
        navigateToGeneralConfiguration();
        // ensure that we are not starting with the email address we using for this test.
        assertTextNotPresent("cancelled.host.com");

        clickLink("general.edit");

        GeneralConfigurationForm form = new GeneralConfigurationForm(tester);
        form.assertFormPresent();

        form.cancelFormElements("cancelled.host.net", "cancelled.help.url", "true", "true");
        form.assertFormNotPresent();

        assertTextNotPresent("cancelled.host.com");
        assertTextNotPresent("cancelled.help.url");
    }


    private void navigateToGeneralConfiguration()
    {
        beginAt("/");
        clickLinkWithText("Administration");
    }

}
