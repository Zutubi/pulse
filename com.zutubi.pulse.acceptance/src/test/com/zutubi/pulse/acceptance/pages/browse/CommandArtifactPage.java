package com.zutubi.pulse.acceptance.pages.browse;

import com.zutubi.pulse.acceptance.SeleniumBrowser;
import com.zutubi.pulse.acceptance.pages.SeleniumPage;
import com.zutubi.pulse.master.webwork.Urls;

/**
 * The decorated artifact page: contains the artifact content with features
 * highlighted.  Only available for plain text artifacts.
 */
public class CommandArtifactPage extends SeleniumPage
{
    private String projectName;
    private long buildId;
    private String stageName;
    private String commandName;
    private String artifactPath;

    public CommandArtifactPage(SeleniumBrowser browser, Urls urls, String projectName, long buildId, String stageName, String commandName, String artifactPath)
    {
        super(browser, urls, "decorated", "artifact " + artifactPath);
        this.projectName = projectName;
        this.buildId = buildId;
        this.stageName = stageName;
        this.commandName = commandName;
        this.artifactPath = artifactPath;
    }

    public String getUrl()
    {
        return urls.commandArtifacts(projectName, Long.toString(buildId), stageName, commandName) + artifactPath;
    }

    public String getProjectName()
    {
        return projectName;
    }

    public long getBuildId()
    {
        return buildId;
    }

    public String getStageName()
    {
        return stageName;
    }

    public String getCommandName()
    {
        return commandName;
    }

    public String getArtifactPath()
    {
        return artifactPath;
    }
}