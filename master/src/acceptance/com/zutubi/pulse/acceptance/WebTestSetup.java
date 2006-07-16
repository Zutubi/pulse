package com.zutubi.pulse.acceptance;

import junit.extensions.TestSetup;
import junit.framework.Test;
import net.sourceforge.jwebunit.WebTester;

/**
 * <class-comment/>
 */
public class WebTestSetup extends TestSetup
{
    protected WebTester tester = null;

    public WebTestSetup(Test test)
    {
        super(test);

        tester = new WebTester();

        // configure tester.
        String port = System.getProperty("pulse.port");
        if (port == null)
        {
            port = "8080";
        }
        tester.getTestContext().setBaseUrl("http://localhost:" + port + "/");
    }

    public void clickLink(String id)
    {
        tester.clickLink(id);
    }

    public void clickLinkWithText(String text)
    {
        tester.clickLinkWithText(text);
    }

    public void beginAt(String relativeUrl)
    {
        tester.beginAt(relativeUrl);
    }

}
