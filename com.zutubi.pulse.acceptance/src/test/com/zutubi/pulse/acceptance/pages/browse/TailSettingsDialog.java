package com.zutubi.pulse.acceptance.pages.browse;

import com.zutubi.pulse.acceptance.SeleniumBrowser;

/**
 * Represents the tail settings popup for log views.
 */
public class TailSettingsDialog
{
    private static final String LOCATOR_APPLY = "css=button:contains('apply')";
    private static final String LOCATOR_CANCEL = "css=button:contains('cancel')";
    private static final String ID_MAX_LINES = "settings-max-lines";

    private SeleniumBrowser browser;

    public TailSettingsDialog(SeleniumBrowser browser)
    {
        this.browser = browser;
    }

    public void waitFor()
    {
        browser.waitForLocator(LOCATOR_APPLY);
    }

    public boolean isVisible()
    {
        return browser.isVisible(LOCATOR_APPLY);
    }

    public void setMaxLines(int max)
    {
        browser.type(ID_MAX_LINES, Integer.toString(max));
    }

    public void clickApply()
    {
        browser.click(LOCATOR_APPLY);
    }

    public void clickCancel()
    {
        browser.click(LOCATOR_CANCEL);
    }
}
