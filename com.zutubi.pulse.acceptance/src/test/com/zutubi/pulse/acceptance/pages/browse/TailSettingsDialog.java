package com.zutubi.pulse.acceptance.pages.browse;

import com.zutubi.pulse.acceptance.SeleniumBrowser;
import org.openqa.selenium.By;

/**
 * Represents the tail settings popup for log views.
 */
public class TailSettingsDialog
{
    private static final String XPATH_APPLY = "//button[contains(text(),'apply')]";
    private static final String XPATH_CANCEL = "//button[contains(text(),'cancel')]";
    private static final String ID_MAX_LINES = "settings-max-lines";

    private SeleniumBrowser browser;

    public TailSettingsDialog(SeleniumBrowser browser)
    {
        this.browser = browser;
    }

    public void waitFor()
    {
        browser.waitForElement(By.xpath(XPATH_APPLY));
    }

    public boolean isVisible()
    {
        return browser.isVisible(By.xpath(XPATH_APPLY));
    }

    public void setMaxLines(int max)
    {
        browser.type(By.id(ID_MAX_LINES), Integer.toString(max));
    }

    public void clickApply()
    {
        browser.click(By.xpath(XPATH_APPLY));
    }

    public void clickCancel()
    {
        browser.click(By.xpath(XPATH_CANCEL));
    }
}
