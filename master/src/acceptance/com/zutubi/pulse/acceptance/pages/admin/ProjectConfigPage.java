package com.zutubi.pulse.acceptance.pages.admin;

import com.thoughtworks.selenium.Selenium;
import com.zutubi.pulse.webwork.mapping.Urls;

/**
 * The page shown when looking at a project in the configuration view.
 */
public class ProjectConfigPage extends ConfigPage
{
    public static final String BUILD_STAGES_BASE    = "stages";
    public static final String BUILD_STAGES_DISPLAY = "build stages";

    private String project;
    private boolean template;

    public ProjectConfigPage(Selenium selenium, Urls urls, String project, boolean template)
    {
        super(selenium, urls, "projects/" + project);
        this.project = project;
        this.template = template;
    }

    public String getUrl()
    {
        return urls.adminProject(project);
    }

    public ProjectHierarchyPage clickHierarchy()
    {
        selenium.click(super.getHierarchyLocator());
        return new ProjectHierarchyPage(selenium, urls, project, template);
    }
}
