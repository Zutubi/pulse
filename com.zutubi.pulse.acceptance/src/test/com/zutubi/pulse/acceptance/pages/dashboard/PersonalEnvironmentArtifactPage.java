package com.zutubi.pulse.acceptance.pages.dashboard;

import com.zutubi.pulse.acceptance.SeleniumBrowser;
import com.zutubi.pulse.acceptance.pages.browse.EnvironmentArtifactPage;
import com.zutubi.pulse.master.webwork.Urls;

/**
 * The decorated artifact page for the command environment artifact.
 */
public class PersonalEnvironmentArtifactPage extends EnvironmentArtifactPage
{
    public PersonalEnvironmentArtifactPage(SeleniumBrowser browser, Urls urls, String projectName, long buildId, String stageName, String commandName)
    {
        super(browser, urls, projectName, buildId, stageName, commandName);
    }

    public String getUrl()
    {
        return urls.myCommandArtifacts(Long.toString(getBuildId()), getStageName(), getCommandName()) + getArtifactPath();
    }
}
