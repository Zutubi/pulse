package com.zutubi.pulse.acceptance;

import com.thoughtworks.selenium.DefaultSelenium;
import com.zutubi.pulse.master.webwork.Urls;

public class ArtifactRepositoryAcceptanceTest extends SeleniumTestBase
{
    protected void setUp() throws Exception
    {
        // the artifact repository is available on a separate port to
        // the main web application, so a custom selenium configuration is required.
        port = "8888";
        baseUrl = "http://localhost:" + port + "/";
        urls = new Urls("");

        String browser = SeleniumUtils.getSeleniumBrowserProperty();

        selenium = new DefaultSelenium("localhost", 4446, browser, baseUrl);
        selenium.start();
    }

    public void testArtifactRepositoryIsAvailable()
    {
        selenium.open("/");
        selenium.waitForPageToLoad("30000");
        assertTextPresent("Directory:");
    }
}
