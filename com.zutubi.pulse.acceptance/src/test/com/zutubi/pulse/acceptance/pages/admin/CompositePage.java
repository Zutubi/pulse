package com.zutubi.pulse.acceptance.pages.admin;

import com.thoughtworks.selenium.Selenium;
import com.zutubi.pulse.acceptance.SeleniumUtils;
import com.zutubi.pulse.master.webwork.Urls;
import com.zutubi.util.StringUtils;

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

    public boolean isConfiguredDescendentPresent(String descendent)
    {
        return selenium.isElementPresent(StringUtils.toValidHtmlName("cd-" + descendent));
    }

    public void clickConfigure()
    {
        selenium.click(CONFIGURE_LINK);
    }

    public String getActionId(String action)
    {
        return "action." + action;
    }

    public boolean isActionPresent(String action)
    {
        return selenium.isElementPresent(getActionId(action));
    }

    public void clickAction(String action)
    {
        selenium.click(getActionId(action));
    }

    public void clickActionAndWait(String action)
    {
        clickAction(action);
        SeleniumUtils.waitForVariable(selenium, "actionInProgress", SeleniumUtils.DEFAULT_TIMEOUT, true);
        waitFor();
    }

    public String getStateId()
    {
        return "config-state";
    }

    public boolean isStatePresent()
    {
        return selenium.isElementPresent(getStateId());
    }

    public String getStateFieldId(String name)
    {
        return "state." + name;
    }

    public boolean isStateFieldPresent(String field)
    {
        return selenium.isElementPresent(getStateFieldId(field));
    }

    public String getStateField(String name)
    {
        return selenium.getText(getStateFieldId(name));
    }

    public String getErrorsId()
    {
        return "nested-errors";
    }

    public boolean areNestedErrorsPresent()
    {
        return selenium.isElementPresent(getErrorsId());
    }

    public boolean isLinksBoxPresent()
    {
        return selenium.isElementPresent("config.links");
    }

    public boolean isLinkPresent(String name)
    {
        return selenium.isElementPresent(getLinkId(name));
    }

    public void clickLink(String name)
    {
        selenium.click(getLinkId(name));
    }

    private String getLinkId(String name)
    {
        return "link." + name;
    }
}
