package com.zutubi.pulse.acceptance.pages;

import com.zutubi.pulse.acceptance.SeleniumBrowser;
import com.zutubi.pulse.master.webwork.Urls;

/**
 */
public class WelcomePage extends SeleniumPage
{
    public WelcomePage(SeleniumBrowser browser, Urls urls)
    {
        super(browser, urls, "welcome.heading", "welcome");
    }

    public String getUrl()
    {
        return urls.base();
    }
}
