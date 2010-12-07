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
    private static final String ID_COLLAPSE_ALL = "toolbar.collapse.all";

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
        String nodeExpression = SeleniumBrowser.CURRENT_WINDOW + ".configTree.getNodeByConfigPath('" + path + "')";
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

    public boolean isTreeLinkVisible(String displayName)
    {
        return browser.isVisible(getTreeLinkLocator(displayName));
    }
    
    public void clickCollapseAll()
    {
        browser.click(ID_COLLAPSE_ALL);
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

    public String getStateId()
    {
        return "config-state";
    }

    public boolean isStatePresent()
    {
        return browser.isElementIdPresent(getStateId());
    }

    public String getStateFieldId(String name)
    {
        return "state." + name;
    }

    public boolean isStateFieldPresent(String field)
    {
        return browser.isElementIdPresent(getStateFieldId(field));
    }

    public String getStateField(String name)
    {
        return browser.getText(getStateFieldId(name));
    }

    public boolean isStateFieldExpandable(String field)
    {
        return browser.isElementIdPresent(getStateFieldExpandId(field));
    }

    public boolean isStateFieldExpandVisible(String field)
    {
        return browser.isVisible(getStateFieldExpandId(field));
    }

    public void expandStateField(String field)
    {
        browser.click(getStateFieldExpandId(field));
        browser.waitForVisible(getStateFieldCollapseId(field));
    }

    public boolean isStateFieldCollapseVisible(String field)
    {
        return browser.isVisible(getStateFieldCollapseId(field));
    }

    public void collapseStateField(String field)
    {
        browser.click(getStateFieldCollapseId(field));
        browser.waitForVisible(getStateFieldExpandId(field));
    }

    private String getStateFieldExpandId(String field)
    {
        return getStateFieldId(field) + ".expand";
    }

    private String getStateFieldCollapseId(String field)
    {
        return getStateFieldId(field) + ".collapse";
    }
}
