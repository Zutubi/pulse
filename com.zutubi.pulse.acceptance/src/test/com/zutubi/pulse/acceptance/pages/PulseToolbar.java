package com.zutubi.pulse.acceptance.pages;

import com.zutubi.pulse.acceptance.SeleniumBrowser;
import com.zutubi.pulse.master.webwork.Urls;
import static com.zutubi.util.WebUtils.uriComponentEncode;

/**
 * The Pulse toolbar is the toolbar at the very top of the Pulse page,
 * containing the breadcrumbs and user related links.
 */
public class PulseToolbar
{
    private static final String ID_TOOLBAR = "pulse-toolbar";
    private static final String ID_PROJECT_LINK = "pulse-toolbar-project-link";
    private static final String ID_BUILD_LINK = "pulse-toolbar-build-link";
    private static final String PREFIX_ID_BUILD_ITEM = "pulse-toolbar-build-item-";
    private static final String ID_AGENT_LINK = "pulse-toolbar-agent-link";
    private static final String ID_BUILD_MENU = "pulse-toolbar_actions_button";
    private static final String ID_NEXT_SUCCESSFUL_LINK = "next-successful-pulse-toolbar_actions";
    private static final String ID_PREVIOUS_SUCCESSFUL_LINK = "previous-successful-pulse-toolbar_actions";
    private static final String ID_NEXT_BROKEN_LINK = "next-broken-pulse-toolbar_actions";
    private static final String ID_PREVIOUS_BROKEN_LINK = "previous-broken-pulse-toolbar_actions";

    private SeleniumBrowser browser;
    private Urls urls;

    public PulseToolbar(SeleniumBrowser browser, Urls urls)
    {
        this.browser = browser;
        this.urls = urls;
    }

    public boolean isPresent()
    {
        return this.browser.isElementIdPresent(ID_TOOLBAR);
    }

    public boolean isBuildNavMenuPresent()
    {
        return this.browser.isElementIdPresent(ID_BUILD_MENU);
    }

    public boolean isNextSuccessfulBuildLinkPresent()
    {
        return this.browser.isElementIdPresent(ID_NEXT_SUCCESSFUL_LINK);
    }

    public void clickNextSuccessfulBuildLink()
    {
        browser.click(ID_NEXT_SUCCESSFUL_LINK);
    }

    public boolean isPreviousSuccessfulBuildLinkPresent()
    {
        return this.browser.isElementIdPresent(ID_PREVIOUS_SUCCESSFUL_LINK);
    }

    public void clickPreviousSuccessfulBuildLink()
    {
        browser.click(ID_PREVIOUS_SUCCESSFUL_LINK);
    }

    public boolean isNextBrokenBuildLinkPresent()
    {
        return this.browser.isElementIdPresent(ID_NEXT_BROKEN_LINK);
    }

    public void clickNextBrokenBuildLink()
    {
        browser.click(ID_NEXT_BROKEN_LINK);
    }

    public boolean isPreviousBrokenBuildLinkPresent()
    {
        return this.browser.isElementIdPresent(ID_PREVIOUS_BROKEN_LINK);
    }

    public void clickPreviousBrokenBuildLink()
    {
        browser.click(ID_PREVIOUS_BROKEN_LINK);
    }

    public void clickOnNavMenu()
    {
        this.browser.click(ID_BUILD_MENU);
    }

    public boolean isBuildNavItemPresent(int buildNumber)
    {
        return browser.isElementIdPresent(PREFIX_ID_BUILD_ITEM + buildNumber);
    }

    public void clickBuildNavItem(int buildNumber)
    {
        browser.click(PREFIX_ID_BUILD_ITEM + buildNumber);
    }

    public boolean isBuildNavLinkPresent(String projectName, int buildNumber)
    {
        return browser.isElementIdPresent(ID_BUILD_LINK) && browser.isLinkToPresent(urls.build(uriComponentEncode(projectName), String.valueOf(buildNumber)));
    }

    public boolean isPersonalBuildNavLinkPresent(int buildNumber)
    {
        return browser.isElementIdPresent(ID_BUILD_LINK) && browser.isLinkToPresent(urls.dashboardMyBuild(String.valueOf(buildNumber)));
    }

    public boolean isProjectLinkPresent(String projectName)
    {
        return browser.isElementIdPresent(ID_PROJECT_LINK) && browser.isLinkToPresent(urls.projectHome(uriComponentEncode(projectName)));
    }

    public boolean isMyBuildsLinkPresent()
    {
        return browser.isElementIdPresent(ID_PROJECT_LINK) && browser.isLinkToPresent(urls.dashboardMyBuilds());
    }

    public void waitForBuildNav()
    {
        browser.waitForElement(ID_BUILD_LINK);
    }

    public boolean isBuildNavPresent()
    {
        return browser.isElementIdPresent(ID_BUILD_LINK);
    }
}
