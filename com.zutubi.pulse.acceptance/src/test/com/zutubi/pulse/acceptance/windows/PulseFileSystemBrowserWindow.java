package com.zutubi.pulse.acceptance.windows;

import com.zutubi.pulse.acceptance.SeleniumBrowser;
import com.zutubi.pulse.core.test.TestUtils;
import com.zutubi.util.Condition;
import org.openqa.selenium.By;

/**
 * A ext popup window containing a tree view of a portion of the
 * pulse file system.
 */
public class PulseFileSystemBrowserWindow
{
    private static final long CLOSE_TIMEOUT = 1000;
    private static final String BROWSER_ID = "pulse-file-system-browser";

    private SeleniumBrowser browser;

    public PulseFileSystemBrowserWindow(SeleniumBrowser browser)
    {
        this.browser = browser;
    }

    public boolean isWindowPresent()
    {
        return browser.isElementIdPresent(BROWSER_ID);
    }

    public void waitForWindow()
    {
        browser.waitForElement(BROWSER_ID);
    }

    public void waitForLoadingToComplete()
    {
        browser.waitForCondition("return Ext.getCmp('" + BROWSER_ID + "').loading === false");
    }

    public void clickOk()
    {
        browser.click(By.xpath(buttonXPath("ok")));
    }

    public void clickCancel()
    {
        browser.click(By.xpath(buttonXPath("cancel")));
    }

    public void waitForClose()
    {
        TestUtils.waitForCondition(new Condition()
        {
            public boolean satisfied()
            {
                return !isWindowPresent();
            }
        }, CLOSE_TIMEOUT, "window to close");
    }

    private String buttonXPath(String buttonText)
    {
        return "//div[@id='" + BROWSER_ID + "']//button[text()='" + buttonText + "']";
    }

    public void selectNode(String path)
    {
        browser.click(By.linkText(path));
    }

    public void waitForNode(String path)
    {
        browser.waitForElement(By.linkText(path));
    }

    public boolean isNodePresent(String path)
    {
        return browser.isElementPresent(By.linkText(path));
    }

    public void doubleClickNode(String path)
    {
        browser.doubleClick(By.linkText(path));
    }

    public void expandPath(String... path)
    {
        for (String p : path)
        {
            waitForNode(p);
            doubleClickNode(p);
        }
    }

    public String getHeader()
    {
        return browser.getText(By.xpath("//div[@id='" + BROWSER_ID + "']//span[contains(@class, 'x-window-header-text')]"));
    }

    public String getStatus()
    {
        return browser.getText(By.xpath("//div[@id='" + BROWSER_ID + "']//div[contains(@class, 'x-status-text')]"));
    }
}
