package com.zutubi.pulse.acceptance.pages.dashboard;

import com.zutubi.pulse.acceptance.SeleniumBrowser;
import com.zutubi.pulse.acceptance.pages.browse.CommandArtifactPage;
import com.zutubi.pulse.master.webwork.Urls;

/**
 * The personal build decorated artifact page: contains the artifact content
 * with features highlighted.  Only available for plain text artifacts.
 */
public class PersonalCommandArtifactPage extends CommandArtifactPage
{
    public PersonalCommandArtifactPage(SeleniumBrowser browser, Urls urls, String projectName, long buildId, String stageName, String commandName, String artifactPath)
    {
        super(browser, urls, projectName, buildId, stageName, commandName, artifactPath);
    }

    public String getUrl()
    {
        return urls.dashboardMyCommandArtifacts(Long.toString(getBuildId()), getStageName(), getCommandName()) + getArtifactPath();
    }
}
