package com.zutubi.pulse.acceptance.pages.admin;

import com.zutubi.pulse.acceptance.SeleniumBrowser;
import com.zutubi.pulse.acceptance.pages.SeleniumPage;
import com.zutubi.pulse.master.webwork.Urls;

/**
 * An abstract base for pages that are shown in the right pane of the
 * configuration UI.
 */
public abstract class ConfigurationPanePage extends SeleniumPage
{
    public ConfigurationPanePage(SeleniumBrowser browser, Urls urls, String id)
    {
        super(browser, urls, id);
    }

    public ConfigurationPanePage(SeleniumBrowser browser, Urls urls, String id, String title)
    {
        super(browser, urls, id, title);
    }

    public void waitFor()
    {
        super.waitFor();
        waitForActionToComplete();
    }

    protected void waitForActionToComplete()
    {
        browser.waitForVariable("actionInProgress", true);
    }
}
