package com.zutubi.pulse.acceptance.pages.browse;

import com.zutubi.pulse.acceptance.SeleniumBrowser;
import com.zutubi.pulse.acceptance.pages.SeleniumPage;
import com.zutubi.pulse.master.webwork.Urls;
import com.zutubi.util.StringUtils;

/**
 * The pulse file tab for a build result.
 */
public class BuildFilePage extends SeleniumPage
{
    private String projectName;
    private long buildId;

    public BuildFilePage(SeleniumBrowser browser, Urls urls, String projectName, long buildId)
    {
        super(browser, urls, StringUtils.uriComponentEncode(projectName) + "-build-" + Long.toString(buildId) + "-file", "build " + buildId);
        this.projectName = projectName;
        this.buildId = buildId;
    }

    public String getUrl()
    {
        return urls.buildFile(projectName, Long.toString(buildId));
    }

    public boolean isHighlightedFilePresent()
    {
        return browser.isElementIdPresent("highlighted.file");
    }

    public void clickDownload()
    {
        browser.click("download.file");
    }
}
