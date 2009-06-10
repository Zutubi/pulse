package com.zutubi.pulse.acceptance.windows;

import com.zutubi.pulse.acceptance.SeleniumBrowser;

/**
 * The browse scm popup window.
 */
public class BrowseScmWindow
{
    private SeleniumBrowser browser;
    private String windowName = "Browse SCM";
    private String originalWindow = null;

    public BrowseScmWindow(SeleniumBrowser browser)
    {
        this.browser = browser;
    }

    public boolean isWindowPresent()
    {
        return browser.isWindowPresent(windowName);
    }

    public void waitForWindow()
    {
        browser.waitForWindow(windowName);
    }

    public void selectWindow()
    {
        waitForWindow();

        originalWindow = browser.getTitle();
        browser.selectWindow(windowName);
    }

    public void clickOkay()
    {
        // would be quicker if we add an id to the field?
        browser.click("//form[@name='form']//input[@value='okay']");
        browser.selectWindow(originalWindow);
        originalWindow = null;
    }

    public void clickCancel()
    {
        // would be quicker if we add an id to the field? 
        browser.click("//form[@name='form']//input[@value='cancel']");
        browser.selectWindow(originalWindow);
        originalWindow = null;
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
