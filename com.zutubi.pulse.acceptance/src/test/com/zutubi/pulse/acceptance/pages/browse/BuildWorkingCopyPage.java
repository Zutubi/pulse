package com.zutubi.pulse.acceptance.pages.browse;

import com.thoughtworks.selenium.Selenium;
import com.zutubi.pulse.acceptance.pages.SeleniumPage;
import com.zutubi.pulse.webwork.mapping.Urls;
import com.zutubi.util.StringUtils;

/**
 * The working copy tab for a build result.
 */
public class BuildWorkingCopyPage extends SeleniumPage
{
    private String projectName;
    private long buildId;

    public BuildWorkingCopyPage(Selenium selenium, Urls urls, String projectName, long buildId)
    {
        super(selenium, urls, StringUtils.uriComponentEncode(projectName) + "-build-" + Long.toString(buildId) + "-wc", "build " + buildId);
        this.projectName = projectName;
        this.buildId = buildId;
    }

    public String getUrl()
    {
        return urls.buildWorkingCopy(projectName, Long.toString(buildId));
    }

    public boolean isWorkingCopyNotPresent()
    {
        return selenium.isElementPresent("wc.not.present");
    }
}
