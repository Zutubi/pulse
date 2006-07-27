package com.zutubi.pulse.acceptance;

import com.zutubi.pulse.acceptance.forms.GeneralConfigurationForm;

/**
 * <class-comment/>
 */
public class GeneralConfigurationAcceptanceTest extends BaseAcceptanceTestCase
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

        loginAsAdmin();
    }

    protected void tearDown() throws Exception
    {
        super.tearDown();
    }

    public void testReset() throws Exception
    {
        navigateToGeneralConfiguration();

        clickLinkWithText("reset");
        assertTextPresent("http://confluence.zutubi.com/display/");
    }

    public void testEdit() throws Exception
    {
        navigateToGeneralConfiguration();
        // ensure that we are not starting with the email address we using for this test.
        assertTextNotPresent("http://saved.base.url.net");
        assertTextNotPresent("saved.help.url");

        clickLink("general.edit");

        GeneralConfigurationForm form = new GeneralConfigurationForm(tester);

        form.assertFormPresent();
        form.saveFormElements("http://saved.base.url.net", "saved.help.url", "true", "true");

        form.assertFormNotPresent();

        assertTextPresent("http://saved.base.url.net");
        assertTextPresent("saved.help.url");

        clickLink("general.edit");
        form.assertFormPresent();
        form.assertFormElements("http://saved.base.url.net", "saved.help.url", "true", "true");
        form.saveFormElements("http://saved.base.url.net", "saved.help.url", "true", "false");
    }

    public void testCancel() throws Exception
    {
        navigateToGeneralConfiguration();
        // ensure that we are not starting with the email address we using for this test.
        assertTextNotPresent("cancelled.host.com");

        clickLink("general.edit");

        GeneralConfigurationForm form = new GeneralConfigurationForm(tester);
        form.assertFormPresent();

        form.cancelFormElements("http://cancelled.base.url.net", "cancelled.help.url", "true", "true");
        form.assertFormNotPresent();

        assertTextNotPresent("http://cancelled.base.url.net");
        assertTextNotPresent("cancelled.help.url");
    }

    public void testValidation() throws Exception
    {
        navigateToGeneralConfiguration();
        clickLink("general.edit");

        GeneralConfigurationForm form = new GeneralConfigurationForm(tester);
        form.assertFormPresent();

        // check that the base url is validated.
        form.saveFormElements("not.a.url", "some.help.url", "true", "false");
        form.assertFormPresent();
        form.assertFormElements("not.a.url", "some.help.url", "true", "false");

        form.saveFormElements("", "some.help.url", "true", "false");
        form.assertFormPresent();
    }

    private void navigateToGeneralConfiguration()
    {
        beginAt("/");
        clickLinkWithText("Administration");
    }

}
