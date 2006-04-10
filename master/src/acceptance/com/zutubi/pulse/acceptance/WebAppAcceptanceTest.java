package com.zutubi.pulse.acceptance;

import com.meterware.httpunit.GetMethodWebRequest;
import com.meterware.httpunit.HttpException;
import org.xml.sax.SAXException;

import java.io.IOException;

/**
 * A set of acceptance tests that ensure the correct behaviour of
 * the web application.
 *
 * ie: no directory browsing is allowed.
 *
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
            String url = createUrl("/admin/");
            tester.getDialog().getWebClient().getResponse(new GetMethodWebRequest(url));
        }
        catch (HttpException e)
        {
            assertEquals(403, e.getResponseCode());
        }
    }

    private String createUrl(String relativeUrl)
    {
        String baseUrl = getTestContext().getBaseUrl();
        if (baseUrl.endsWith("/") && relativeUrl.startsWith("/"))
        {
            return baseUrl + relativeUrl.substring(1);
        }
        return baseUrl + relativeUrl;
    }
}
