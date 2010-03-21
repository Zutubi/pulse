package com.zutubi.pulse.acceptance.windows;

import com.zutubi.pulse.acceptance.SeleniumBrowser;

public class PulseFileSystemBrowserWindow
{
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

    public void clickOk()
    {
        // would be quicker if we add an id to the field?
        browser.click("//div[@id='"+BROWSER_ID+"']//button[text()='ok']");
    }

    public void clickCancel()
    {
        // would be quicker if we add an id to the field? 
        browser.click("//div[@id='"+BROWSER_ID+"']//button[text()='cancel']");
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
        return "link="+linkText+"";
    }
}
