package com.zutubi.pulse.acceptance.pages.browse;

import com.zutubi.pulse.acceptance.SeleniumBrowser;
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

    public AbstractTestsPage(SeleniumBrowser browser, Urls urls, String id, String title)
    {
        super(browser, urls, id, title);
    }

    public TestResultSummary getTestSummary()
    {
        int total = Integer.parseInt(browser.getCellContents(ID_TEST_SUMMARY, 2, 2));
        int failures = Integer.parseInt(browser.getCellContents(ID_TEST_SUMMARY, 2, 3));
        int errors = Integer.parseInt(browser.getCellContents(ID_TEST_SUMMARY, 2, 4));
        int skipped = Integer.parseInt(browser.getCellContents(ID_TEST_SUMMARY, 2, 5));

        return new TestResultSummary(errors, failures, skipped, total);
    }

    public void clickAllCrumb()
    {
        browser.click(ID_ALL_CRUMB);
    }

    public void clickStageCrumb()
    {
        browser.click(ID_STAGE_CRUMB);
    }

    public void clickSuiteCrumb(String suitePath)
    {
        browser.click(StringUtils.toValidHtmlName("suitecrumb-" + suitePath));
    }

    public void clickSuiteLink(String suite)
    {
        browser.click(StringUtils.toValidHtmlName("suite-" + suite));
    }
}
