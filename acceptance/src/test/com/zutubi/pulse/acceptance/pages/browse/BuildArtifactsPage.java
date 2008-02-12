package com.zutubi.pulse.acceptance.pages.browse;

import com.thoughtworks.selenium.Selenium;
import com.zutubi.pulse.acceptance.pages.SeleniumPage;
import com.zutubi.pulse.webwork.mapping.Urls;
import com.zutubi.util.StringUtils;

/**
 * The artifacts tab for a build result.
 */
public class BuildArtifactsPage extends SeleniumPage
{
    private String projectName;
    private long buildId;

    public BuildArtifactsPage(Selenium selenium, Urls urls, String projectName, long buildId)
    {
        super(selenium, urls, StringUtils.uriComponentEncode(projectName) + "-build-" + Long.toString(buildId) + "-artifacts", "build " + buildId);
        this.projectName = projectName;
        this.buildId = buildId;
    }

    public String getUrl()
    {
        return urls.buildArtifacts(projectName, Long.toString(buildId));
    }

    public String getCommandLocator(String command)
    {
        return "link=*command*::*" + command + "*";
    }
}
