package com.zutubi.pulse.acceptance.pages.admin;

import com.zutubi.pulse.acceptance.IDs;
import com.zutubi.pulse.acceptance.SeleniumBrowser;
import com.zutubi.pulse.master.webwork.Urls;
import com.zutubi.tove.type.record.PathUtils;

/**
 * Base for config pages that appear when the "configuration" tab is selected
 * in the tree pane.
 */
public abstract class ConfigPage extends ConfigurationPanePage
{
    public ConfigPage(SeleniumBrowser browser, Urls urls, String id)
    {
        super(browser, urls, id);
    }

    public ConfigPage(SeleniumBrowser browser, Urls urls, String id, String title)
    {
        super(browser, urls, id, title);
    }

    public abstract String getPath();

    public CompositePage clickComposite(String relativePath, String displayName)
    {
        browser.click(getTreeLinkLocator(displayName));
        return browser.createPage(CompositePage.class, PathUtils.getPath(getPath(), relativePath));
    }

    public ListPage clickCollection(String baseName, String displayName)
    {
        browser.click(getTreeLinkLocator(displayName));
        return browser.createPage(ListPage.class, PathUtils.getPath(getId(), baseName));
    }

    public void expandTreeNode(String path)
    {
        String nodeExpression = "selenium.browserbot.getCurrentWindow().configTree.getNodeByConfigPath('" + path + "')";
        browser.evalExpression(nodeExpression + ".expand(false, false);");
        browser.waitForCondition(nodeExpression + ".isExpanded();");
    }

    public String getTreeLinkLocator(String displayName)
    {
        return "//div[@id='config-tree']//a[span='" + displayName + "']";
    }

    public boolean isTreeLinkPresent(String displayName)
    {
        return browser.isElementPresent(getTreeLinkLocator(displayName));
    }

    protected String getHierarchyLocator()
    {
        return "//span[text()='hierarchy']";
    }

    public boolean isCollapsedCollectionPresent()
    {
        return browser.isElementIdPresent(IDs.COLLECTION_TABLE);
    }

    public boolean isLinksBoxPresent()
    {
        return browser.isElementIdPresent(IDs.LINKS_BOX);
    }
}
