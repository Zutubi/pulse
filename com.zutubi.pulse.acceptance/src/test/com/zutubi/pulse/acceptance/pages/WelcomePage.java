package com.zutubi.pulse.acceptance.pages;

import com.thoughtworks.selenium.Selenium;
import com.zutubi.pulse.master.webwork.Urls;

/**
 */
public class WelcomePage extends SeleniumPage
{
    public WelcomePage(Selenium selenium, Urls urls)
    {
        super(selenium, urls, "welcome.heading", "welcome");
    }

    public String getUrl()
    {
        return urls.base();
    }
}
