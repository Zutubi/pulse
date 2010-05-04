package com.zutubi.pulse.acceptance.pages.browse;

import com.zutubi.pulse.acceptance.SeleniumBrowser;
import com.zutubi.pulse.acceptance.windows.PulseFileSystemBrowserWindow;
import static com.zutubi.pulse.master.tove.config.project.ProjectConfigurationActions.*;
import com.zutubi.pulse.master.webwork.Urls;
import static com.zutubi.util.WebUtils.uriComponentEncode;

/**
 * The project home page is a summary of the state and recent activity for a
 * project.
 */
public class ProjectHomePage extends ResponsibilityPage
{
    private String projectName;

    public ProjectHomePage(SeleniumBrowser browser, Urls urls, String projectName)
    {
        super(browser, urls, "project-home-" + projectName, uriComponentEncode(projectName));
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
        return urls.project(uriComponentEncode(projectName));
    }
}
