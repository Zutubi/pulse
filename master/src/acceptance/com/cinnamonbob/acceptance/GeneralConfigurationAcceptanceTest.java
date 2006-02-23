package com.cinnamonbob.acceptance;

/**
 * <class-comment/>
 */
public class GeneralConfigurationAcceptanceTest extends ExtendedWebTestCase
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
    }

    protected void tearDown() throws Exception
    {
        super.tearDown();
    }

    public void testReset() throws Exception
    {
        navigateToGeneralConfiguration();

        clickLinkWithText("reset");
        assertTextPresent("localhost:8080");
    }

    public void testEdit() throws Exception
    {
        navigateToGeneralConfiguration();
        // ensure that we are not starting with the email address we using for this test.
        assertTextNotPresent("saved.host.net");

        clickLinkWithText("edit");
        setFormElement("hostName", "saved.host.net");
        submit("save");

        assertTextPresent("saved.host.net");
    }

    public void testCancel() throws Exception
    {
        navigateToGeneralConfiguration();
        // ensure that we are not starting with the email address we using for this test.
        assertTextNotPresent("cancelled.host.com");

        clickLinkWithText("edit");
        setFormElement("hostName", "cancelled.host.com");
        submit("cancel");

        assertTextNotPresent("cancelled.host.com");
    }


    private void navigateToGeneralConfiguration()
    {
        beginAt("/");
        clickLinkWithText("Administration");
        clickLinkWithText("general configuration");
    }

}
