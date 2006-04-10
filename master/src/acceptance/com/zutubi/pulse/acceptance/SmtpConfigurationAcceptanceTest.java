package com.zutubi.pulse.acceptance;

/**
 * <class-comment/>
 */
public class SmtpConfigurationAcceptanceTest extends BaseAcceptanceTest
{
    public SmtpConfigurationAcceptanceTest()
    {
    }

    public SmtpConfigurationAcceptanceTest(String name)
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
        navigateToSmtpConfiguration();

        clickLink("smtp.reset");
        assertTextPresent("bob@sensorynetworks.com");
        assertTextPresent("smtp.people.net.au");
    }

    private void navigateToSmtpConfiguration()
    {
        beginAt("/");
        clickLinkWithText("administration");
    }

    public void testEdit() throws Exception
    {
        navigateToSmtpConfiguration();
        // ensure that we are not starting with the email address we using for this test.
        assertTextNotPresent("someone@somewhere.net");

        clickLink("smtp.edit");
        setFormElement("smtp.from", "someone@somewhere.net");
        submit("save");

        assertTextPresent("someone@somewhere.net");
    }

    public void testCancel() throws Exception
    {
        navigateToSmtpConfiguration();
        // ensure that we are not starting with the email address we using for this test.
        assertTextNotPresent("cancelled@action.com");

        clickLink("smtp.edit");
        setFormElement("smtp.from", "cancelled@action.com");
        submit("cancel");

        assertTextNotPresent("cancelled@action.com");
    }


}
