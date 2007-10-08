package com.zutubi.pulse.acceptance.pages.admin;

import com.thoughtworks.selenium.Selenium;
import com.zutubi.pulse.acceptance.pages.SeleniumPage;
import com.zutubi.pulse.webwork.mapping.Urls;
import com.zutubi.prototype.type.record.PathUtils;

/**
 * The page shown when looking at a project in the configuration view.
 */
public class ProjectConfigPage extends SeleniumPage
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

    public void assertPresent()
    {
        super.assertPresent();
    }

    public String getUrl()
    {
        return urls.adminProject(project);
    }

    public ListPage selectCollection(String baseName, String displayName)
    {
        selenium.click("link=" + displayName);
        ListPage listPage = new ListPage(selenium, urls, PathUtils.getPath(getId(), baseName));
        listPage.waitFor();
        return listPage;
    }
}
