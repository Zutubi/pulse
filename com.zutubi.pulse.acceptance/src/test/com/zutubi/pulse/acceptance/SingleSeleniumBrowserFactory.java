package com.zutubi.pulse.acceptance;

/**
 * The single selenium browser factory will only create a single browser
 * and return the same instance on request via {@link #newBrowser()}.
 *
 * When {@link #cleanup()} is called, the browser will be logged out and
 * reset to '/'.  When {@link #stop()} is called, the browser will actually
 * be shutdown.
 *
 * The benefit of this browser factory is the overhead of repeated requests
 * for a browser instance is greatly reduced.  But care must be taken as
 * the browser persists between tests.
 */
public class SingleSeleniumBrowserFactory implements SeleniumBrowserFactory
{
    private static SeleniumBrowser browser;

    public synchronized SeleniumBrowser newBrowser()
    {
        if (browser == null)
        {
            browser = new SeleniumBrowser();
            browser.start();

        }
        return browser;
    }

    public synchronized void cleanup()
    {
        if (browser != null)
        {
            browser.open("/");
            browser.waitForPageToLoad();
            
            if (browser.isLoggedIn())
            {
                browser.logout();
            }
            
            browser.open("/");
            browser.waitForPageToLoad();
        }
    }

    public void stop()
    {
        cleanup();
        if (browser != null)
        {
            browser.stop();
            browser = null;
        }
    }
}
