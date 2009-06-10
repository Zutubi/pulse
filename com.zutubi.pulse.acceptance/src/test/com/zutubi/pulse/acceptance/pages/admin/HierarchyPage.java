package com.zutubi.pulse.acceptance.pages.admin;

import com.zutubi.pulse.acceptance.SeleniumBrowser;
import com.zutubi.pulse.master.webwork.Urls;
import com.zutubi.tove.type.record.PathUtils;

/**
 * The page shown when looking at the heirarchy view of a templated scope.
 */
public class HierarchyPage extends ConfigurationPanePage
{
    public static final String LINK_ADD = "add.new";
    public static final String LINK_ADD_TEMPLATE = "add.template";
    public static final String LINK_CLONE = "clone";
    public static final String LINK_CONFIGURE = "configure";
    public static final String LINK_DELETE = "delete";
    public static final String LINK_SMART_CLONE = "smartclone";

    protected String scope;
    protected String baseName;
    protected boolean template;

    public HierarchyPage(SeleniumBrowser browser, Urls urls, String scope, String baseName, boolean template)
    {
        super(browser, urls, PathUtils.getPath(scope, baseName));
        this.scope = scope;
        this.baseName = baseName;
        this.template = template;
    }

    public boolean isAddLinkPresent()
    {
        return isLinkPresent(LINK_ADD);
    }

    public boolean isAddTempalteLinkPresent()
    {
        return isLinkPresent(LINK_ADD_TEMPLATE);
    }

    public boolean isConfigureLinkPresent()
    {
        return isLinkPresent(LINK_CONFIGURE);
    }

    public boolean isTemplate()
    {
        return template;
    }
    
    protected boolean isLinkPresent(String link)
    {
        return browser.isLinkPresent(link);
    }

    public String getUrl()
    {
        return urls.admin() + scope + "/";
    }

    public String getTreeItemLocator(String baseName)
    {
        return "link=" + baseName;
    }

    public void open()
    {
        browser.open(getUrl());
        // Allow the initial page load.
        browser.waitForPageToLoad();
    }

    public void waitFor()
    {
        // Wait for the default right panel to load.
        waitForActionToComplete();

        // Choose our panel and wait for it.
        String linkLocator = getTreeItemLocator(baseName);
        browser.waitForLocator(linkLocator);
        browser.click(linkLocator);
        super.waitFor();
    }

    public boolean isTreeItemPresent(String baseName)
    {
        return browser.isElementPresent(getTreeItemLocator(baseName));
    }

    public boolean isTreeItemVisible(String baseName)
    {
        return browser.isVisible(getTreeItemLocator(baseName));
    }

    public void expandTreeItem(String baseName)
    {
        browser.doubleClick(getTreeItemLocator(baseName));
    }

    public boolean isAddPresent()
    {
        return browser.isElementIdPresent(LINK_ADD);
    }

    public void clickAdd()
    {
        browser.waitAndClick(LINK_ADD);
    }

    public void clickAddTemplate()
    {
        browser.waitAndClick(LINK_ADD_TEMPLATE);
    }

    public boolean isClonePresent()
    {
        return browser.isElementIdPresent(LINK_CLONE);
    }

    public void clickClone()
    {
        browser.waitAndClick(LINK_CLONE);
    }

    public void clickSmartClone()
    {
        browser.waitAndClick(LINK_SMART_CLONE);
    }

    public void setTemplate(boolean template)
    {
        this.template = template;
    }

    public DeleteConfirmPage clickDelete()
    {
        browser.waitAndClick(LINK_DELETE);
        return browser.createPage(DeleteConfirmPage.class, getId(), false);
    }
}
