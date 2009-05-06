package com.zutubi.pulse.acceptance.pages.browse;

import com.thoughtworks.selenium.Selenium;
import com.zutubi.pulse.acceptance.SeleniumUtils;
import com.zutubi.pulse.acceptance.pages.ProjectsSummaryPage;
import com.zutubi.pulse.master.webwork.Urls;

/**
 * The browse page is the default in the browse section and shows a list of
 * projects, including the latest build results of each.
 */
public class BrowsePage extends ProjectsSummaryPage
{
    public BrowsePage(Selenium selenium, Urls urls)
    {
        super(selenium, urls, "projects", "projects");
    }

    public String getUrl()
    {
        return urls.projects();
    }

    @Override
    public void waitFor()
    {
        super.waitFor();
        SeleniumUtils.waitForVariable(selenium, "view.initialised", SeleniumUtils.DEFAULT_TIMEOUT);
    }
}
