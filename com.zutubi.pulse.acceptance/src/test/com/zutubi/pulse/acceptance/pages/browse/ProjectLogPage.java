package com.zutubi.pulse.acceptance.pages.browse;

import com.zutubi.pulse.acceptance.SeleniumBrowser;
import com.zutubi.pulse.acceptance.pages.SeleniumPage;
import com.zutubi.pulse.master.webwork.Urls;
import com.zutubi.util.StringUtils;

/**
 * The browse project log page.
 */
public class ProjectLogPage extends SeleniumPage
{
    private String projectName;

    public ProjectLogPage(SeleniumBrowser browser, Urls urls, String projectName)
    {
        super(browser, urls, "project-log-" + StringUtils.uriComponentEncode(projectName));
        this.projectName = projectName;
    }

    public String getUrl()
    {
        return urls.projectLog(projectName);
    }

    public boolean isDownloadLinkAvailable()
    {
        return browser.isElementIdPresent("link=full log");
    }

    public void clickDownloadLink()
    {
        browser.click("link=full log");
    }

    /**
     * Retrieve the visible log text
     *
     * @return log text.
     */
    public String getLog()
    {
        return browser.getText("project-log-" + projectName);
    }

    public boolean logContains(String text)
    {
        return getLog().contains(text);
    }
}
