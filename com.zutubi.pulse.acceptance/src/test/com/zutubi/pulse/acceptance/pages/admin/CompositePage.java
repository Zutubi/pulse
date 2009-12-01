package com.zutubi.pulse.acceptance.pages.admin;

import com.zutubi.pulse.acceptance.SeleniumBrowser;
import com.zutubi.pulse.master.webwork.Urls;
import com.zutubi.util.WebUtils;
import static com.zutubi.util.WebUtils.uriPathEncode;

/**
 * A page in the admin UI that displays a single composite.  This page
 * carries a form when the config exists or is configurable and writable.  It
 * also displays state, navigation and action links in various circumstances.
 */
public class CompositePage extends ConfigPage
{
    private static final String CONFIGURE_LINK = "configure";

    private String path;

    public CompositePage(SeleniumBrowser browser, Urls urls, String path)
    {
        super(browser, urls, path);
        this.path = path;
    }

    public String getPath()
    {
        return path;
    }

    public String getUrl()
    {
        return urls.admin() + uriPathEncode(path) + "/";
    }

    public boolean isConfigureLinkPresent()
    {
        return browser.isLinkPresent(CONFIGURE_LINK);
    }

    public boolean isConfiguredDescendentPresent(String descendent)
    {
        return browser.isElementIdPresent(WebUtils.toValidHtmlName("cd-" + descendent));
    }

    public void clickConfigure()
    {
        browser.click(CONFIGURE_LINK);
    }

    public String getActionId(String action)
    {
        return "action." + action;
    }

    public boolean isActionPresent(String action)
    {
        return browser.isElementIdPresent(getActionId(action));
    }

    public void clickAction(String action)
    {
        browser.click(getActionId(action));
    }

    public void clickActionAndWait(String action)
    {
        clickAction(action);
        waitForAction();
    }

    public boolean isDescendentActionsPresent()
    {
        return browser.isElementPresent("descendent.actions");
    }

    public String getDescendentActionId(String action)
    {
        return "descendent.action." + action;
    }

    public boolean isDescendentActionPresent(String action)
    {
        return browser.isElementPresent(getDescendentActionId(action));
    }

    public void clickDescendentAction(String action)
    {
        browser.click(getDescendentActionId(action));
    }

    public void clickDescendentActionAndWait(String action)
    {
        clickDescendentAction(action);
        waitForAction();
    }

    private void waitForAction()
    {
        browser.waitForVariable("actionInProgress", true);
        waitFor();
    }

    public String getErrorsId()
    {
        return "nested-errors";
    }

    public boolean areNestedErrorsPresent()
    {
        return browser.isElementIdPresent(getErrorsId());
    }

    public boolean isLinksBoxPresent()
    {
        return browser.isElementIdPresent("config.links");
    }

    public boolean isLinkPresent(String name)
    {
        return browser.isElementIdPresent(getLinkId(name));
    }

    public void clickLink(String name)
    {
        browser.click(getLinkId(name));
    }

    private String getLinkId(String name)
    {
        return "link." + name;
    }
}
