package com.zutubi.pulse.acceptance.pages;

import com.zutubi.pulse.acceptance.SeleniumBrowser;
import com.zutubi.pulse.master.webwork.Urls;

/**
 * The Pulse toolbar is the toolbar at the very top of the Pulse page,
 * containing the breadcrumbs and user related links.
 */
public class PulseToolbar
{
    private static final String TOOLBAR_ID = "pulse-toolbar";
    private static final String PROJECT_LINK_ID = "pulse-toolbar-project-link";
    private static final String BUILD_LINK_ID = "pulse-toolbar-build-link";
    private static final String BUILD_ITEM_ID_PREFIX = "pulse-toolbar-build-item-";
    private static final String AGENT_LINK_ID = "pulse-toolbar-agent-link";
    private static final String BUILD_MENU_ID = "pulse-toolbar_actions_button";
    private static final String NEXT_SUCCESSFUL_LINK_ID = "next-successful-pulse-toolbar_actions";
    private static final String PREVIOUS_SUCCESSFUL_LINK_ID = "previous-successful-pulse-toolbar_actions";
    private static final String NEXT_BROKEN_LINK_ID = "next-broken-pulse-toolbar_actions";
    private static final String PREVIOUS_BROKEN_LINK_ID = "previous-broken-pulse-toolbar_actions";

    private SeleniumBrowser browser;
    private Urls urls;

    public PulseToolbar(SeleniumBrowser browser, Urls urls)
    {
        this.browser = browser;
        this.urls = urls;
    }

    public boolean isPresent()
    {
        return this.browser.isElementIdPresent(TOOLBAR_ID);
    }

    public boolean isBuildNavMenuPresent()
    {
        return this.browser.isElementIdPresent(BUILD_MENU_ID);
    }

    public boolean isNextSuccessfulBuildLinkPresent()
    {
        return this.browser.isElementIdPresent(NEXT_SUCCESSFUL_LINK_ID);
    }

    public boolean isPreviousSuccessfulBuildLinkPresent()
    {
        return this.browser.isElementIdPresent(PREVIOUS_SUCCESSFUL_LINK_ID);
    }

    public boolean isNextBrokenBuildLinkPresent()
    {
        return this.browser.isElementIdPresent(NEXT_BROKEN_LINK_ID);
    }

    public boolean isPreviousBrokenBuildLinkPresent()
    {
        return this.browser.isElementIdPresent(PREVIOUS_BROKEN_LINK_ID);
    }

    public void clickOnNavMenu()
    {
        this.browser.click(BUILD_MENU_ID);
    }

    public boolean isBuildNavItemPresent(int buildNumber)
    {
        return browser.isElementIdPresent(BUILD_ITEM_ID_PREFIX + buildNumber);
    }

    public boolean isBuildNavLinkPresent(String projectName, int buildNumber)
    {
        return browser.isElementIdPresent(BUILD_LINK_ID) && browser.isLinkToPresent(getBaseUrl() + urls.build(projectName, String.valueOf(buildNumber)));
    }

    public boolean isProjectLinkPresent(String projectName)
    {
        return browser.isElementIdPresent(PROJECT_LINK_ID) && browser.isLinkToPresent(getBaseUrl() + urls.projectHome(projectName));
    }

    public void waitForBuildNav()
    {
        browser.waitForElement(BUILD_LINK_ID);
    }

    public boolean isBuildNavPresent()
    {
        return browser.isElementIdPresent(BUILD_LINK_ID);
    }

    private String getBaseUrl()
    {
        String baseUrl = browser.getBaseUrl();
        if (baseUrl.endsWith("/"))
        {
            baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
        }
        return baseUrl;
    }
}
