package com.zutubi.pulse.acceptance.pages.admin;

import com.thoughtworks.selenium.Selenium;
import com.zutubi.prototype.type.record.PathUtils;
import com.zutubi.pulse.acceptance.SeleniumUtils;
import com.zutubi.pulse.webwork.mapping.Urls;
import com.zutubi.util.CollectionUtils;
import junit.framework.Assert;

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

    protected String scope;
    protected String baseName;
    protected boolean template;

    public HierarchyPage(Selenium selenium, Urls urls, String scope, String baseName, boolean template)
    {
        super(selenium, urls, PathUtils.getPath(scope, baseName));
        this.scope = scope;
        this.baseName = baseName;
        this.template = template;
    }

    public void assertPresent()
    {
        super.assertPresent();

        String[] links = selenium.getAllLinks();
        if (template)
        {
            Assert.assertTrue(CollectionUtils.contains(links, LINK_ADD));
            Assert.assertTrue(CollectionUtils.contains(links, LINK_ADD_TEMPLATE));
        }

        Assert.assertTrue(CollectionUtils.contains(links, LINK_CONFIGURE));
    }

    public String getUrl()
    {
        return urls.admin() + scope + "/";
    }

    public String getTreeItemLocator(String baseName)
    {
        return "link=" + baseName;
    }

    public void goTo()
    {
        selenium.open(getUrl());
        String linkLocator = getTreeItemLocator(baseName);
        SeleniumUtils.waitForLocator(selenium, linkLocator);
        selenium.click(linkLocator);
        waitFor();
    }

    public boolean isTreeItemPresent(String baseName)
    {
        return selenium.isElementPresent(getTreeItemLocator(baseName));
    }

    public boolean isTreeItemVisible(String baseName)
    {
        return selenium.isVisible(getTreeItemLocator(baseName));
    }

    public void expandTreeItem(String baseName)
    {
        selenium.doubleClick(getTreeItemLocator(baseName));
    }

    public boolean isAddPresent()
    {
        return selenium.isElementPresent(LINK_ADD);
    }

    public void clickAdd()
    {
        SeleniumUtils.waitAndClickId(selenium, LINK_ADD);
    }

    public void clickAddTemplate()
    {
        SeleniumUtils.waitAndClickId(selenium, LINK_ADD_TEMPLATE);
    }

    public boolean isClonePresent()
    {
        return selenium.isElementPresent(LINK_CLONE);
    }

    public void clickClone()
    {
        SeleniumUtils.waitAndClickId(selenium, LINK_CLONE);
    }

    public void setTemplate(boolean template)
    {
        this.template = template;
    }

    public DeleteConfirmPage clickDelete()
    {
        SeleniumUtils.waitAndClickId(selenium, LINK_DELETE);
        return new DeleteConfirmPage(selenium, urls, getId(), false);
    }
}
