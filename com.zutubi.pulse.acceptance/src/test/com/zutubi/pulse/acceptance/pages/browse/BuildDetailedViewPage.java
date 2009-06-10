package com.zutubi.pulse.acceptance.pages.browse;

import com.thoughtworks.selenium.Selenium;
import com.zutubi.pulse.acceptance.SeleniumUtils;
import com.zutubi.pulse.acceptance.pages.SeleniumPage;
import com.zutubi.pulse.master.webwork.Urls;
import com.zutubi.util.StringUtils;

/**
 * The detailed view tab for a build result.
 */
public class BuildDetailedViewPage extends SeleniumPage
{
    private String projectName;
    private long buildId;
    
    public BuildDetailedViewPage(Selenium selenium, Urls urls, String projectName, long buildId)
    {
        super(selenium, urls, StringUtils.uriComponentEncode(projectName) + "-build-" + Long.toString(buildId) + "-detailed", StringUtils.uriComponentEncode(projectName));
        this.projectName = projectName;
        this.buildId = buildId;
    }

    public String getUrl()
    {
        return urls.buildDetails(projectName, Long.toString(buildId));
    }

    public void clickStage(String stageName)
    {
        selenium.click("xpath=//li[@id='stage-" + stageName + "']/a");
    }

    public void clickCommand(String stageName, String commandName)
    {
        selenium.click("xpath=//li[@id='stage-" + stageName + "-command-" + commandName + "']/a");
    }

    public String getCustomFieldsId(String stageName)
    {
        return "custom.fields." + stageName;
    }

    public String getCustomFieldValue(String stageName, int index)
    {
        return SeleniumUtils.getCellContents(selenium, getCustomFieldsId(stageName), index + 1, 1);
    }
}
