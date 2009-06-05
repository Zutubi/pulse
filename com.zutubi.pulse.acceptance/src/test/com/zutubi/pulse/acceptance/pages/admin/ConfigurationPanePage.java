package com.zutubi.pulse.acceptance.pages.admin;

import com.thoughtworks.selenium.Selenium;
import com.zutubi.pulse.acceptance.SeleniumUtils;
import com.zutubi.pulse.acceptance.pages.SeleniumPage;
import com.zutubi.pulse.master.webwork.Urls;

/**
 * An abstract base for pages that are shown in the right pane of the
 * configuration UI.
 */
public abstract class ConfigurationPanePage extends SeleniumPage
{
    public ConfigurationPanePage(Selenium selenium, Urls urls, String id)
    {
        super(selenium, urls, id);
    }

    public ConfigurationPanePage(Selenium selenium, Urls urls, String id, String title)
    {
        super(selenium, urls, id, title);
    }

    public void waitFor()
    {
        super.waitFor();
        waitForActionToComplete();
    }

    protected void waitForActionToComplete()
    {
        SeleniumUtils.waitForVariable(selenium, "actionInProgress", SeleniumUtils.DEFAULT_TIMEOUT, true);
    }
}
