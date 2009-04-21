package com.zutubi.pulse.acceptance.windows;

import com.thoughtworks.selenium.Selenium;
import com.zutubi.pulse.acceptance.SeleniumUtils;
import static com.zutubi.pulse.core.test.TestUtils.waitForCondition;
import com.zutubi.util.Condition;

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
        waitForCondition(new Condition()
        {
            public boolean satisfied()
            {
                return isWindowPresent();
            }
        }, SeleniumUtils.DEFAULT_TIMEOUT, "Timeout waiting for " + windowName + " window.");
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

    public void selectNode(String path)
    {
        selenium.click(toSelector(path));
    }

    public void waitForNode(String path)
    {
        SeleniumUtils.waitForLocator(selenium, toSelector(path));
    }

    public boolean isNodePresent(String path)
    {
        return selenium.isElementPresent(toSelector(path));
    }

    public void doubleClickNode(String path)
    {
        selenium.doubleClick(toSelector(path));
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
