package com.zutubi.pulse.acceptance.pages;

import com.zutubi.pulse.acceptance.SeleniumBrowser;

/**
 * Base class for dialogs created by Ext.
 */
public class MessageDialog
{
    private static final String LOCATOR_INPUT_SINGLE = "css=input.ext-mb-input";
    private static final String LOCATOR_INPUT_MULTI = "css=textarea.ext-mb-textarea";
    private static final String LOCATOR_OK = "css=button:contains('OK')";
    private static final String LOCATOR_CANCEL = "css=button:contains('Cancel')";

    private SeleniumBrowser browser;
    private boolean multiline;

    public MessageDialog(SeleniumBrowser browser, boolean multiline)
    {
        this.browser = browser;
        this.multiline = multiline;
    }

    public void waitFor()
    {
        browser.waitForLocator(LOCATOR_OK);
    }

    public boolean isVisible()
    {
        return browser.isVisible(LOCATOR_OK);
    }

    public void typeInput(String text)
    {
        browser.type(getInputLocator(), text);
    }

    private String getInputLocator()
    {
        return multiline ? LOCATOR_INPUT_MULTI : LOCATOR_INPUT_SINGLE;
    }

    public void clickOk()
    {
        browser.click(LOCATOR_OK);
    }

    public void clickCancel()
    {
        browser.click(LOCATOR_CANCEL);
    }
}