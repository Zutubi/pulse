package com.zutubi.pulse.acceptance.pages.admin;

import com.thoughtworks.selenium.Selenium;
import com.zutubi.pulse.acceptance.SeleniumUtils;
import com.zutubi.pulse.webwork.mapping.Urls;

/**
 * A page in the admin UI that displays a single composite.  This page
 * carries a form when the config exists or is configurable and writable.  It
 * also displays state, navigation and action links in various circumstances.
 */
public class CompositePage extends ConfigPage
{
    private static final String CONFIGURE_LINK = "configure";

    private String path;

    public CompositePage(Selenium selenium, Urls urls, String path)
    {
        super(selenium, urls, path);
        this.path = path;
    }

    public String getPath()
    {
        return path;
    }

    public String getUrl()
    {
        return urls.admin() + path + "/";
    }

    public boolean isConfigureLinkPresent()
    {
        return SeleniumUtils.isLinkPresent(selenium, CONFIGURE_LINK);
    }

    public void assertConfiguredDescendents(String... descendents)
    {
        for (String descendent: descendents)
        {
            SeleniumUtils.assertElementPresent(selenium, "cd-" + descendent);
        }
    }
}
