package com.zutubi.pulse.acceptance.pages.dashboard;

import com.thoughtworks.selenium.Selenium;
import com.zutubi.pulse.acceptance.pages.browse.EnvironmentArtifactPage;
import com.zutubi.pulse.master.webwork.Urls;

/**
 * The decorated artifact page for the command environment artifact.
 */
public class PersonalEnvironmentArtifactPage extends EnvironmentArtifactPage
{
    public PersonalEnvironmentArtifactPage(Selenium selenium, Urls urls, String projectName, long buildId, String stageName, String commandName)
    {
        super(selenium, urls, projectName, buildId, stageName, commandName);
    }

    public String getUrl()
    {
        return urls.dashboardMyCommandArtifacts(Long.toString(getBuildId()), getStageName(), getCommandName()) + getArtifactPath();
    }
}
