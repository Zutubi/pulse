package com.cinnamonbob.acceptance;

public class AcceptanceTest extends ExtendedWebTestCase
{
    public AcceptanceTest(String name)
    {
        super(name);
    }

    public void setUp()
    {
        getTestContext().setBaseUrl("http://www.google.com");
    }

    public void testSearch()
    {
        beginAt("/");
        setFormElement("q", "httpunit");
        submit("btnG");
        clickLinkWithText("HttpUnit");
        assertTitleEquals("HttpUnit Home");
        assertLinkPresentWithText("User's Manual");
    }
}