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
        form.saveFormElements("http://saved.base.url.net", "saved.help.url", "true", "false", "false", "4", "true", "0");

        form.assertFormNotPresent();

        assertTextPresent("http://saved.base.url.net");
        assertTextPresent("saved.help.url");
        assertTextPresent("after 0 minutes");

        clickLink("general.edit");
        form.assertFormPresent();
        form.assertFormElements("http://saved.base.url.net", "saved.help.url", "true", "false", "false", "4", "true", "0");
    }

    public void testCancel() throws Exception
    {
        navigateToGeneralConfiguration();
        // ensure that we are not starting with the email address we using for this test.
        assertTextNotPresent("cancelled.host.com");

        clickLink("general.edit");

        GeneralConfigurationForm form = new GeneralConfigurationForm(tester);
        form.assertFormPresent();

        form.cancelFormElements("http://cancelled.base.url.net", "cancelled.help.url", "true", "false", "false", "5", "true", "100");
        form.assertFormNotPresent();

        assertTextNotPresent("http://cancelled.base.url.net");
        assertTextNotPresent("cancelled.help.url");
        assertTextNotPresent("after 100 minutes");
    }

    public void testValidation() throws Exception
    {
        navigateToGeneralConfiguration();
        clickLink("general.edit");

        GeneralConfigurationForm form = new GeneralConfigurationForm(tester);
        form.assertFormPresent();

        // check that the base url is validated.
        form.saveFormElements("not.a.url", "some.help.url", "true", "false", "false", "5", "true", "15");
        form.assertFormPresent();
        form.assertFormElements("not.a.url", "some.help.url", "true", "false", "false", "5", "true", "15");

        form.saveFormElements("", "some.help.url", "true", "false", "false", "5", "true", "15");
        form.assertFormPresent();

        // check that the scm polling interval is 0 <
        form.saveFormElements("http://base.url.com", "some.help.url", "true", "false", "false", "0", "true", "15");
        form.assertFormPresent();

        form.saveFormElements("http://base.url.com", "some.help.url", "true", "false", "false", "-30", "true", "15");
        form.assertFormPresent();

        form.saveFormElements("http://base.url.com", "some.help.url", "true", "false", "false", "a", "true", "15");
        form.assertFormPresent();

        // Test negative timeout
        form.saveFormElements("http://base.url.com", "some.help.url", "true", "false", "false", "a", "true", "-1");
        form.assertFormPresent();
        assertTextPresent("timeout value must not be negative");
    }

}
