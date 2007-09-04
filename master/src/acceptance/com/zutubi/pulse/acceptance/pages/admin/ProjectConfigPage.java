package com.zutubi.pulse.acceptance.pages.admin;

import com.thoughtworks.selenium.Selenium;
import com.zutubi.pulse.acceptance.pages.SeleniumPage;
import com.zutubi.pulse.webwork.mapping.Urls;

/**
 * The page shown when looking at a project in the configuration view.
 */
public class ProjectConfigPage extends SeleniumPage
{
    private String project;
    private boolean template;

    public ProjectConfigPage(Selenium selenium, Urls urls, String project, boolean template)
    {
        super(selenium, urls, "projects/" + project);
        this.project = project;
        this.template = template;
    }

    public void assertPresent()
    {
        super.assertPresent();
    }

    public String getUrl()
    {
        return urls.adminProject(project);
    }
}
