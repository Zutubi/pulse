package com.zutubi.pulse.acceptance.pages.browse;

import com.thoughtworks.selenium.Selenium;
import com.zutubi.pulse.acceptance.pages.SeleniumPage;
import com.zutubi.pulse.webwork.mapping.Urls;
import com.zutubi.util.StringUtils;

/**
 * The pulse file tab for a build result.
 */
public class BuildFilePage extends SeleniumPage
{
    private String projectName;
    private long buildId;

    public BuildFilePage(Selenium selenium, Urls urls, String projectName, long buildId)
    {
        super(selenium, urls, StringUtils.uriComponentEncode(projectName) + "-build-" + Long.toString(buildId) + "-file", "build " + buildId);
        this.projectName = projectName;
        this.buildId = buildId;
    }

    public String getUrl()
    {
        return urls.buildFile(projectName, Long.toString(buildId));
    }

    public boolean isHighlightedFilePresent()
    {
        return selenium.isElementPresent("highlighted.file");
    }

    public void clickDownload()
    {
        selenium.click("download.file");
    }
}
