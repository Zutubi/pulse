package com.zutubi.pulse.acceptance;

/**
 * <class-comment/>
 */
public class SmtpConfigurationAcceptanceTest extends BaseAcceptanceTestCase
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
        loginAsAdmin();
    }

    protected void tearDown() throws Exception
    {
        super.tearDown();
    }

    public void testReset() throws Exception
    {
        navigateToSmtpConfiguration();

        clickLink("smtp.reset");

        assertTablePresent("smtp.config");
        assertTableRowsEqual("smtp.config", 1, new String[][]{
                new String[]{"smtp host"},
                new String[]{"subject prefix"},
                new String[]{"from address"},
                new String[]{"username"}
        });

    }

    public void testEdit() throws Exception
    {
        navigateToSmtpConfiguration();
        // ensure that we are not starting with the email address we using for this test.
        assertTextNotPresent("someone@somewhere.net");

        clickLink("smtp.edit");
        setFormElement("smtp.host", "myhost");
        setFormElement("smtp.from", "someone@somewhere.net");
        submit("save");

        assertTablePresent("smtp.config");
        assertTextPresent("myhost");
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
