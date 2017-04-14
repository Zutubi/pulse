/* Copyright 2017 Zutubi Pty Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.zutubi.pulse.acceptance;

import com.zutubi.pulse.acceptance.forms.LoginForm;
import com.zutubi.pulse.acceptance.pages.LoginPage;
import com.zutubi.util.logging.Logger;
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
    public static final Logger LOG = Logger.getLogger(SingleSeleniumBrowserFactory.class);

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
                        try
                        {
                            // Give it one more shot.
                            returnToLogin();
                        }
                        catch (Exception ee)
                        {
                            LOG.severe("Unable to return to login, forcing new browser session.");
                            browser.quit();
                            browser = null;
                        }
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
            browser.createForm(LoginForm.class).waitFor();
        }
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
