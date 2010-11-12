package com.zutubi.pulse.acceptance.pages.dashboard;

import com.zutubi.pulse.acceptance.SeleniumBrowser;
import com.zutubi.pulse.acceptance.pages.SeleniumPage;
import com.zutubi.pulse.master.webwork.Urls;

public class MyPreferencesPage extends SeleniumPage
{
    public MyPreferencesPage(SeleniumBrowser browser, Urls urls)
    {
        super(browser, urls, "tab.dashboard.preferences");
    }

    @Override
    public boolean isPresent()
    {
        return browser.isElementPresent("//a[@id=\"tab.dashboard.preferences\" and contains(@class, 'active')]");
    }

    public String getUrl()
    {
        return urls.dashboard() + "/preferences/";
    }
}
