package com.zutubi.pulse.acceptance.pages.browse;

import com.thoughtworks.selenium.Selenium;
import com.zutubi.pulse.acceptance.SeleniumUtils;
import com.zutubi.pulse.acceptance.pages.SeleniumPage;
import com.zutubi.pulse.core.model.TestResultSummary;
import com.zutubi.pulse.master.webwork.Urls;
import com.zutubi.util.StringUtils;

/**
 */
public abstract class AbstractTestsPage extends SeleniumPage
{
    protected static final String ID_TEST_SUMMARY = "test.summary";
    protected static final String ID_ALL_CRUMB = "allcrumb";
    protected static final String ID_STAGE_CRUMB = "stagecrumb";

    public AbstractTestsPage(Selenium selenium, Urls urls, String id, String title)
    {
        super(selenium, urls, id, title);
    }

    public TestResultSummary getTestSummary()
    {
        int total = Integer.parseInt(SeleniumUtils.getCellContents(selenium, ID_TEST_SUMMARY, 2, 2));
        int failures = Integer.parseInt(SeleniumUtils.getCellContents(selenium, ID_TEST_SUMMARY, 2, 3));
        int errors = Integer.parseInt(SeleniumUtils.getCellContents(selenium, ID_TEST_SUMMARY, 2, 4));
        int skipped = Integer.parseInt(SeleniumUtils.getCellContents(selenium, ID_TEST_SUMMARY, 2, 5));

        return new TestResultSummary(errors, failures, skipped, total);
    }

    public void clickAllCrumb()
    {
        selenium.click(ID_ALL_CRUMB);
    }

    public void clickStageCrumb()
    {
        selenium.click(ID_STAGE_CRUMB);
    }

    public void clickSuiteCrumb(String suitePath)
    {
        selenium.click(StringUtils.toValidHtmlName("suitecrumb-" + suitePath));
    }

    public void clickSuiteLink(String suite)
    {
        selenium.click(StringUtils.toValidHtmlName("suite-" + suite));
    }
}
