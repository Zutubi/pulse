package com.zutubi.pulse.acceptance.pages.browse;

import com.zutubi.pulse.acceptance.pages.SeleniumPage;
import com.zutubi.pulse.master.webwork.Urls;
import com.zutubi.util.StringUtils;
import com.thoughtworks.selenium.Selenium;

/**
 * The browse project log page.
 */
public class ProjectLogPage extends SeleniumPage
{
    private String projectName;

    public ProjectLogPage(Selenium selenium, Urls urls, String projectName)
    {
        super(selenium, urls, "project-log-" + StringUtils.uriComponentEncode(projectName));
        this.projectName = projectName;
    }

    public String getUrl()
    {
        return urls.projectLog(projectName);
    }

    public boolean isDownloadLinkAvailable()
    {
        return selenium.isElementPresent("link=full log");
    }

    public void clickDownloadLink()
    {
        selenium.click("link=full log");
    }

    /**
     * Retrieve the visible log text
     *
     * @return log text.
     */
    public String getLog()
    {
        return selenium.getText("project-log-" + projectName);
    }

    public boolean logContains(String text)
    {
        return getLog().contains(text);
    }
}
