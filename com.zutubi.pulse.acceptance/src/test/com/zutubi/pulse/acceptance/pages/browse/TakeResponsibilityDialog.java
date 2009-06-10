package com.zutubi.pulse.acceptance.pages.browse;

import com.zutubi.pulse.acceptance.SeleniumBrowser;

/**
 * Represents the popup prompt show when taking responsibilty for a build.
 */
public class TakeResponsibilityDialog
{
    private static final String LOCATOR_COMMENT = "css=input.ext-mb-input";
    private static final String LOCATOR_OK = "css=button:contains('OK')";

    private SeleniumBrowser browser;

    public TakeResponsibilityDialog(SeleniumBrowser browser)
    {
        this.browser = browser;
    }

    public void waitFor()
    {
        browser.waitForLocator(LOCATOR_COMMENT);
    }

    public void typeComment(String comment)
    {
        browser.type(LOCATOR_COMMENT, comment);
    }

    public void clickOk()
    {
        browser.click(LOCATOR_OK);
    }
}
