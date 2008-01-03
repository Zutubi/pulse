package com.zutubi.pulse.acceptance.pages.admin;

import com.thoughtworks.selenium.Selenium;
import com.zutubi.prototype.type.record.PathUtils;
import com.zutubi.pulse.acceptance.SeleniumUtils;
import com.zutubi.pulse.acceptance.pages.SeleniumPage;
import com.zutubi.pulse.webwork.mapping.Urls;
import com.zutubi.util.CollectionUtils;
import junit.framework.Assert;

/**
 * The page shown when looking at the heirarchy view of a templated scope.
 */
public class HierarchyPage extends SeleniumPage
{
    public static final String LINK_ADD = "add.new";
    public static final String LINK_ADD_TEMPLATE = "add.template";
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

    public void goTo()
    {
        selenium.open(getUrl());
        String linkLocator = "link=" + baseName;
        SeleniumUtils.waitForLocator(selenium, linkLocator);
        selenium.click(linkLocator);
        waitFor();
    }

    public boolean isAddPresent()
    {
        return selenium.isElementPresent(LINK_ADD);
    }

    public void clickAdd()
    {
        selenium.click(LINK_ADD);
    }

    public void clickAddTemplate()
    {
        selenium.click(LINK_ADD_TEMPLATE);
    }

    public void setTemplate(boolean template)
    {
        this.template = template;
    }

    public DeleteConfirmPage clickDelete()
    {
        selenium.click(LINK_DELETE);
        return new DeleteConfirmPage(selenium, urls, getId(), false);
    }
}
