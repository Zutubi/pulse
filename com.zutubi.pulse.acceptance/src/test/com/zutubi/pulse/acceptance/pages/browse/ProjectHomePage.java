package com.zutubi.pulse.acceptance.pages.browse;

import com.zutubi.pulse.acceptance.SeleniumBrowser;
import com.zutubi.pulse.acceptance.windows.PulseFileSystemBrowserWindow;
import static com.zutubi.pulse.master.tove.config.project.ProjectConfigurationActions.ACTION_REBUILD;
import static com.zutubi.pulse.master.tove.config.project.ProjectConfigurationActions.ACTION_TRIGGER;
import static com.zutubi.pulse.master.tove.config.project.ProjectConfigurationActions.ACTION_VIEW_SOURCE;
import com.zutubi.pulse.master.webwork.Urls;
import com.zutubi.util.WebUtils;

/**
 * The project home page is a summary of the state and recent activity for a
 * project.
 */
public class ProjectHomePage extends ResponsibilityPage
{
    private String projectName;

    public ProjectHomePage(SeleniumBrowser browser, Urls urls, String projectName)
    {
        super(browser, urls, "project-home-" + WebUtils.uriComponentEncode(projectName), WebUtils.uriComponentEncode(projectName));
        this.projectName = projectName;
    }

    public void triggerBuild()
    {
        clickAction(ACTION_TRIGGER);
    }

    public boolean isTriggerActionPresent()
    {
        return isActionPresent(ACTION_TRIGGER);
    }

    public boolean isRebuildActionPresent()
    {
        return isActionPresent(ACTION_REBUILD);
    }

    public boolean isViewWorkingCopyPresent()
    {
        return isActionPresent(ACTION_VIEW_SOURCE);
    }

    public PulseFileSystemBrowserWindow viewWorkingCopy()
    {
        clickAction(ACTION_VIEW_SOURCE);
        return new PulseFileSystemBrowserWindow(browser);
    }

    public String getUrl()
    {
        return urls.project(projectName);
    }
}
