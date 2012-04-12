package com.zutubi.pulse.acceptance.pages;

import com.zutubi.pulse.acceptance.SeleniumBrowser;
import org.openqa.selenium.By;

/**
 * Base class for dialogs created by Ext.
 */
public class MessageDialog
{
    private static final String SELECTOR_INPUT_SINGLE = "input.ext-mb-input";
    private static final String SELECTOR_INPUT_MULTI = "textarea.ext-mb-textarea";
    private static final String XPATH_OK = "//button[contains(text(),'ok')]";
    private static final String XPATH_CANCEL = "//button[contains(text(),'cancel')]";

    private SeleniumBrowser browser;
    private boolean multiline;

    public MessageDialog(SeleniumBrowser browser, boolean multiline)
    {
        this.browser = browser;
        this.multiline = multiline;
    }

    public void waitFor()
    {
        browser.waitForElement(By.xpath(XPATH_OK));
    }

    public boolean isVisible()
    {
        return browser.isVisible(By.xpath(XPATH_OK));
    }

    public void typeInput(String text)
    {
        browser.type(By.cssSelector(getInputSelector()), text);
    }

    private String getInputSelector()
    {
        return multiline ? SELECTOR_INPUT_MULTI : SELECTOR_INPUT_SINGLE;
    }

    public void clickOk()
    {
        browser.click(By.xpath(XPATH_OK));
    }

    public void clickCancel()
    {
        browser.click(By.xpath(XPATH_CANCEL));
    }
}