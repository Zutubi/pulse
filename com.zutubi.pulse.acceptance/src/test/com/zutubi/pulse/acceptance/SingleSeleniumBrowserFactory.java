package com.zutubi.pulse.acceptance;

import com.zutubi.pulse.acceptance.forms.LoginForm;
import com.zutubi.pulse.acceptance.pages.LoginPage;
import org.openqa.selenium.remote.UnreachableBrowserException;

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
        }
        return browser;
    }

    public synchronized void cleanup()
    {
        if (browser != null)
        {
            try
            {
                if (browser.getPulsePort() == AcceptanceTestUtils.getPulsePort())
                {
                    // Reuse this browser.
                    try
                    {
                        returnToLogin();
                    }
                    catch (Exception e)
                    {
                        // Give it one more shot.
                        returnToLogin();
                    }
                }
                else
                {
                    // Force creation of a new browser.
                    browser.quit();
                    browser = null;
                }
            }
            catch (UnreachableBrowserException e)
            {
                // The browser looks to have died of its own accord.
                browser = null;
            }
        }
    }

    private void returnToLogin()
    {
        if (browser.isLoggedIn())
        {
            browser.logout();
        }
        else
        {
            browser.createPage(LoginPage.class).open();
        }

        browser.createForm(LoginForm.class).waitFor();
    }

    public void stop()
    {
        cleanup();
        if (browser != null)
        {
            browser.quit();
            browser = null;
        }
    }
}
