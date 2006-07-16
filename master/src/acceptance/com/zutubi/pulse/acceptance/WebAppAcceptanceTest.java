package com.zutubi.pulse.acceptance;

import com.meterware.httpunit.HttpException;
import com.meterware.httpunit.HttpNotFoundException;
import org.xml.sax.SAXException;

import java.io.IOException;

/**
 * A set of acceptance tests that ensure the correct behaviour of the web application.
 * ie: no directory browsing is allowed.
 */
public class WebAppAcceptanceTest extends BaseAcceptanceTest
{
    public WebAppAcceptanceTest()
    {
    }

    public WebAppAcceptanceTest(String name)
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

    public void testDirectoryListingIsNotAllowed() throws IOException, SAXException
    {
        try
        {
            // ensure that the jwebunit and httpunit components are initialised.
            beginAt("/");

            // request the page via the httpunit component so that we get the
            // full response details.
            goTo("/admin/");
        }
        catch (HttpException e)
        {
            assertEquals(403, e.getResponseCode());
        }
    }

    public void test404DisplaysAllInformation() throws IOException, SAXException
    {
        // log in.
        loginAsAdmin();

        String badAction = "anactionthatdoesnotexist";
        try
        {
            beginAt("/");
            goTo("/" + badAction + ".action");
        }
        catch (HttpNotFoundException e)
        {
            assertEquals(404, e.getResponseCode());
            assertTrue(e.getMessage().indexOf(badAction) != -1);
        }
    }
}
