package com.zutubi.pulse.acceptance.pages.admin;

import com.thoughtworks.selenium.Selenium;
import com.zutubi.pulse.acceptance.pages.SeleniumPage;
import com.zutubi.util.CollectionUtils;
import junit.framework.Assert;

/**
 * The page shown when looking at a project in the heirarchy view.
 */
public class ProjectHierarchyPage extends SeleniumPage
{
    public static final String LINK_ADD = "add.new";
    public static final String LINK_ADD_TEMPLATE = "add.template";
    public static final String LINK_CONFIGURE = "configure";
    public static final String LINK_DELETE = "delete";

    private boolean template;

    public ProjectHierarchyPage(Selenium selenium, String project, boolean template)
    {
        super(selenium, "projects/" + project);
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

    public void clickAdd()
    {
        selenium.click(LINK_ADD);
    }

    public void setTemplate(boolean template)
    {
        this.template = template;
    }
}
