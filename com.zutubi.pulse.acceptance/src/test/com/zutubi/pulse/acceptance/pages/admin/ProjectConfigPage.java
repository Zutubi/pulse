package com.zutubi.pulse.acceptance.pages.admin;

import com.thoughtworks.selenium.Selenium;
import com.zutubi.pulse.master.webwork.mapping.Urls;

/**
 * The page shown when looking at a project in the configuration view.
 */
public class ProjectConfigPage extends CompositePage
{
    public static final String BUILD_STAGES_BASE    = "stages";
    public static final String BUILD_STAGES_DISPLAY = "build stages";

    private String project;
    private boolean template;

    public ProjectConfigPage(Selenium selenium, Urls urls, String project, boolean template)
    {
        super(selenium, urls, getPath(project));
        this.project = project;
        this.template = template;
    }

    private static String getPath(String project)
    {
        return "projects/" + project;
    }

    public String getPath()
    {
        return getPath(project);
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
