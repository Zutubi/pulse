package com.zutubi.pulse.acceptance.windows;

import com.zutubi.pulse.acceptance.SeleniumBrowser;
import com.zutubi.pulse.core.test.TestUtils;
import com.zutubi.util.Condition;

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
        return browser.isElementPresent(BROWSER_ID);
    }

    public void waitForWindow()
    {
        browser.waitForElement(BROWSER_ID);
    }

    public void waitForLoadingToComplete()
    {
        browser.waitForCondition(SeleniumBrowser.CURRENT_WINDOW + ".Ext.getCmp('" + BROWSER_ID + "').loading === false");
    }

    public void clickOk()
    {
        browser.click(buttonLocator("ok"));
    }

    public void clickCancel()
    {
        browser.click(buttonLocator("cancel"));
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

    private String buttonLocator(String buttonText)
    {
        return "//div[@id='" + BROWSER_ID + "']//button[text()='" + buttonText + "']";
    }

    public void selectNode(String path)
    {
        browser.click(toSelector(path));
    }

    public void waitForNode(String path)
    {
        browser.waitForLocator(toSelector(path));
    }

    public boolean isNodePresent(String path)
    {
        return browser.isElementPresent(toSelector(path));
    }

    public void doubleClickNode(String path)
    {
        browser.doubleClick(toSelector(path));
    }

    public void expandPath(String... path)
    {
        for (String p : path)
        {
            waitForNode(p);
            doubleClickNode(p);
        }
    }

    private String toSelector(String linkText)
    {
        return "link=" + linkText + "";
    }

    public String getHeader()
    {
        return browser.getText("//div[@id='" + BROWSER_ID + "']//span[contains(@class, 'x-window-header-text')]");
    }

    public String getStatus()
    {
        return browser.getText("//div[@id='" + BROWSER_ID + "']//div[contains(@class, 'x-status-text')]");
    }
}
