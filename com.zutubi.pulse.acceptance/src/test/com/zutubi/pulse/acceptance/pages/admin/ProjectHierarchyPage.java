package com.zutubi.pulse.acceptance.pages.admin;

import com.zutubi.pulse.acceptance.SeleniumBrowser;
import com.zutubi.pulse.master.tove.config.MasterConfigurationRegistry;
import com.zutubi.pulse.master.webwork.Urls;

/**
 * The page shown when looking at a project in the heirarchy view.
 */
public class ProjectHierarchyPage extends HierarchyPage
{
    public ProjectHierarchyPage(SeleniumBrowser browser, Urls urls, String project, boolean template)
    {
        super(browser, urls, MasterConfigurationRegistry.PROJECTS_SCOPE, project, template);
    }

    public ProjectConfigPage clickConfigure()
    {
        browser.click(LINK_CONFIGURE);
        return browser.createPage(ProjectConfigPage.class, baseName, template);
    }
}
