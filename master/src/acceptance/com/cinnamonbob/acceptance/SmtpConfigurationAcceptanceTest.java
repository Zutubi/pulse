package com.cinnamonbob.acceptance;

/**
 * <class-comment/>
 */
public class SmtpConfigurationAcceptanceTest extends ExtendedWebTestCase
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
    }

    protected void tearDown() throws Exception
    {
        super.tearDown();
    }

    public void testReset() throws Exception
    {
        navigateToSmtpConfiguration();

        clickLinkWithText("reset", 1);
        assertTextPresent("bob@sensorynetworks.com");
        assertTextPresent("smtp.people.net.au");
    }

    private void navigateToSmtpConfiguration()
    {
        beginAt("/");
        clickLinkWithText("Administration");
    }

    public void testEdit() throws Exception
    {
        navigateToSmtpConfiguration();
        // ensure that we are not starting with the email address we using for this test.
        assertTextNotPresent("someone@somewhere.net");

        clickLinkWithText("edit", 1);
        setFormElement("smtp.from", "someone@somewhere.net");
        submit("save");

        assertTextPresent("someone@somewhere.net");
    }

    public void testCancel() throws Exception
    {
        navigateToSmtpConfiguration();
        // ensure that we are not starting with the email address we using for this test.
        assertTextNotPresent("cancelled@action.com");

        clickLinkWithText("edit", 1);
        setFormElement("smtp.from", "cancelled@action.com");
        submit("cancel");

        assertTextNotPresent("cancelled@action.com");
    }


}
