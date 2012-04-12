package com.zutubi.pulse.acceptance.pages.browse;

import com.zutubi.pulse.acceptance.SeleniumBrowser;
import com.zutubi.pulse.master.webwork.Urls;
import com.zutubi.tove.type.record.PathUtils;
import com.zutubi.util.WebUtils;
import org.openqa.selenium.By;

/**
 * The tests page for a specific suite result.
 */
public class TestSuitePage extends AbstractTestsPage
{
    public static final String FILTER_NONE = "";
    public static final String FILTER_BROKEN = "broken";

    private static final String ID_COMBO = "filter-combo";

    private String projectName;
    private String stageName;
    private String suitePath;
    private long buildId;

    public TestSuitePage(SeleniumBrowser browser, Urls urls, String projectName, long buildId, String stageName, String suitePath)
    {
        super(browser, urls, projectName + "-build-" + Long.toString(buildId) + "-tests-" + stageName + "-" + suitePath, "build " + buildId);
        this.projectName = projectName;
        this.stageName = stageName;
        this.suitePath = suitePath;
        this.buildId = buildId;
    }

    public String getUrl()
    {
        return urls.stageTests(WebUtils.uriComponentEncode(projectName), Long.toString(buildId), stageName) + suitePath;
    }

    public TestSuitePage clickSuiteAndWait(String suiteName)
    {
        clickSuiteLink(suiteName);
        return browser.waitFor(TestSuitePage.class, projectName, buildId, stageName, PathUtils.getPath(suitePath, WebUtils.uriComponentEncode(suiteName)));
    }

    public String getCurrentFilter()
    {
        return (String) browser.evaluateScript("return Ext.getCmp('" + ID_COMBO + "').getValue();");
    }

    public void setFilterAndWait(String filter)
    {
        browser.setComboByValue(ID_COMBO, filter);
        browser.waitForCondition("return filtering === false");
    }

    public boolean isTestCaseVisible(String caseName)
    {
        return browser.isVisible(By.xpath("//td[text()='" + caseName + "']"));
    }
}