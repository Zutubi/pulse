package com.zutubi.pulse.acceptance.pages.browse;

import com.zutubi.pulse.acceptance.SeleniumBrowser;
import com.zutubi.pulse.acceptance.pages.SeleniumPage;
import com.zutubi.pulse.master.webwork.Urls;
import com.zutubi.util.WebUtils;
import org.openqa.selenium.By;

/**
 * The pulse file tab for a build result.
 */
public class BuildFilePage extends SeleniumPage
{
    private static final String ID_DOWNLOAD_LINK = "download-file";
    private static final String ID_HIGHLIGHTED_FILE = "highlighted-file";

    private String projectName;
    private long buildId;

    public BuildFilePage(SeleniumBrowser browser, Urls urls, String projectName, long buildId)
    {
        super(browser, urls, projectName + "-build-" + Long.toString(buildId) + "-file", "build " + buildId);
        this.projectName = projectName;
        this.buildId = buildId;
    }

    public String getUrl()
    {
        return urls.buildFile(WebUtils.uriComponentEncode(projectName), Long.toString(buildId));
    }

    public boolean isHighlightedFilePresent()
    {
        return browser.isElementIdPresent(ID_HIGHLIGHTED_FILE);
    }

    public boolean isDownloadLinkPresent()
    {
        return browser.isElementIdPresent(ID_DOWNLOAD_LINK);
    }

    public void clickDownload()
    {
        browser.click(By.id(ID_DOWNLOAD_LINK));
    }
}
