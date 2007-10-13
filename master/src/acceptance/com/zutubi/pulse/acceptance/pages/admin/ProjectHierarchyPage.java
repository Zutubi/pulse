package com.zutubi.pulse.acceptance.pages.admin;

import com.thoughtworks.selenium.Selenium;
import com.zutubi.pulse.acceptance.SeleniumUtils;
import com.zutubi.pulse.acceptance.pages.SeleniumPage;
import com.zutubi.pulse.webwork.mapping.Urls;
import com.zutubi.util.CollectionUtils;
import com.zutubi.prototype.config.ConfigurationRegistry;
import junit.framework.Assert;

/**
 * The page shown when looking at a project in the heirarchy view.
 */
public class ProjectHierarchyPage extends HierarchyPage
{
    public ProjectHierarchyPage(Selenium selenium, Urls urls, String project, boolean template)
    {
        super(selenium, urls, ConfigurationRegistry.PROJECTS_SCOPE, project, template);
    }

    public ProjectConfigPage clickConfigure()
    {
        selenium.click(LINK_CONFIGURE);
        return new ProjectConfigPage(selenium, urls, baseName, template);
    }
}
