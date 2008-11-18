package com.zutubi.pulse.acceptance.windows;

import com.thoughtworks.selenium.Selenium;
import com.thoughtworks.selenium.SeleniumException;
import com.zutubi.pulse.acceptance.SeleniumUtils;

import java.util.Arrays;

/**
 * The browse scm popup window.
 */
public class BrowseScmWindow
{
    private Selenium selenium;
    private String windowName = "Browse SCM";
    private String originalWindow = null;

    public BrowseScmWindow(Selenium selenium)
    {
        this.selenium = selenium;
    }

    public boolean isWindowPresent()
    {
        return Arrays.asList(selenium.getAllWindowTitles()).contains(windowName);
    }

    public void waitForWindow()
    {
        int timeout = SeleniumUtils.DEFAULT_TIMEOUT;

        long startTime = System.currentTimeMillis();
        while (System.currentTimeMillis() - startTime < timeout)
        {
            if (isWindowPresent())
            {
                return;
            }

            try
            {
                Thread.sleep(300);
            }
            catch (InterruptedException e)
            {
                // Ignore
            }
        }
        throw new SeleniumException("Timeout out after " + timeout + "ms");
    }

    public void selectWindow()
    {
        waitForWindow();

        originalWindow = selenium.getTitle();
        selenium.selectWindow(windowName);
    }

    public void clickOkay()
    {
        // would be quicker if we add an id to the field?
        selenium.click("//form[@name='form']//input[@value='okay']");
        selenium.selectWindow(originalWindow);
        originalWindow = null;
    }

    public void clickCancel()
    {
        // would be quicker if we add an id to the field? 
        selenium.click("//form[@name='form']//input[@value='cancel']");
        selenium.selectWindow(originalWindow);
        originalWindow = null;
    }

    /**
     * Click the text of the node in the browse tree that is identified by the specified
     * path.
     *
     * @param path  the path to the selected node.
     */
    public void selectNode(String... path)
    {
        selenium.click("//div[@id='browseTree']" + toXpathSelector(path));
    }

    public void waitForNode(String... path)
    {
        SeleniumUtils.waitForLocator(selenium, "//div[@id='browseTree']" + toXpathSelector(path));
    }

    public boolean isNodePresent(String... path)
    {
        return selenium.isElementPresent("//div[@id='browseTree']" + toXpathSelector(path));
    }

    /**
     * Double click the text of the node in the browse tree identified by the specified path.
     *
     * @param path  the path to the selected tree node
     */
    public void doubleClickNode(String... path)
    {
        selenium.doubleClick("//div[@id='browseTree']" + toXpathSelector(path));
    }

    private String toXpathSelector(String... path)
    {
        String selector = "";
        for (String p : path)
        {
            selector = selector + "//a[text()='"+p+"']";
        }
        return selector;
    }

}
