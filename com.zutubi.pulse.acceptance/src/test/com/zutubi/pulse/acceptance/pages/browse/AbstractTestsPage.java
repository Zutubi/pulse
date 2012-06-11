package com.zutubi.pulse.acceptance.pages.browse;

import com.zutubi.pulse.acceptance.SeleniumBrowser;
import com.zutubi.pulse.acceptance.pages.SeleniumPage;
import com.zutubi.pulse.core.model.TestResultSummary;
import com.zutubi.pulse.master.webwork.Urls;
import com.zutubi.util.WebUtils;
import org.openqa.selenium.By;

/**
 */
public abstract class AbstractTestsPage extends SeleniumPage
{
    protected static final String ID_TEST_SUMMARY = "test-summary";
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
        int expectedFailures = Integer.parseInt(browser.getCellContents(ID_TEST_SUMMARY, 2, 5));
        int skipped = Integer.parseInt(browser.getCellContents(ID_TEST_SUMMARY, 2, 6));

        return new TestResultSummary(expectedFailures, errors, failures, skipped, total);
    }

    public void clickAllCrumb()
    {
        clickCrumb(ID_ALL_CRUMB);
    }

    public void clickStageCrumb()
    {
        clickCrumb(ID_STAGE_CRUMB);
    }

    public void clickSuiteCrumb(String suitePath)
    {
        clickCrumb(WebUtils.toValidHtmlName("suitecrumb-" + suitePath));
    }

    private void clickCrumb(String id)
    {
        By crumbLocator = By.xpath("//span[@id='" + id + "']/a");
        browser.waitForElement(crumbLocator);
        browser.click(crumbLocator);
    }

    public void clickSuiteLink(String suite)
    {
        browser.click(By.id(WebUtils.toValidHtmlName("suite-" + suite)));
    }
}
