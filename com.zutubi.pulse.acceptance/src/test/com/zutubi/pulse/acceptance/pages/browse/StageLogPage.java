package com.zutubi.pulse.acceptance.pages.browse;

import com.zutubi.pulse.acceptance.SeleniumBrowser;
import com.zutubi.pulse.master.webwork.Urls;

/**
 * A page that represents the stage log page.
 */
public class StageLogPage extends AbstractLogPage
{
    protected String project;
    protected String buildNumber;
    protected String stageName;

    public StageLogPage(SeleniumBrowser browser, Urls urls, String projectName, long buildNumber, String stageName)
    {
        super(browser, urls, "stage-log-" + projectName + "-" + buildNumber + "-" + stageName);
        this.project = projectName;
        this.buildNumber = String.valueOf(buildNumber);
        this.stageName = stageName;
    }

    public String getUrl()
    {
        return urls.stageLog(project, buildNumber, stageName);
    }
}
