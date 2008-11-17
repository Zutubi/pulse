package com.zutubi.pulse.acceptance.windows;

import com.thoughtworks.selenium.Selenium;
import com.zutubi.pulse.acceptance.SeleniumUtils;

import java.util.Arrays;

import junit.framework.TestCase;

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

    public void assertPresent()
    {
        TestCase.assertTrue(isWindowPresent());
    }

    public void assertNotPresent()
    {
        TestCase.assertFalse(isWindowPresent());
    }

    private boolean isWindowPresent()
    {
        return Arrays.asList(selenium.getAllWindowTitles()).contains(windowName);
    }

    public void selectWindow()
    {
        assertPresent();
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
     * Click the text of the node in the browse tree that contains the given text
     *
     * @param text  the text of the selected browse tree node
     */
    public void selectNode(String text)
    {
        selenium.click("//div[@id='browseTree']//a[text()='"+text+"']");
    }

    public void waitForNode(String text)
    {
        SeleniumUtils.waitForLocator(selenium, "//div[@id='browseTree']//a[text()='"+text+"']");
    }

    public boolean isNodePresent(String text)
    {
        return selenium.isElementPresent("//div[@id='browseTree']//a[text()='"+text+"']");
    }

    public void assertNodePresent(String text)
    {
        TestCase.assertTrue(isNodePresent(text));
    }

    public void assertNodeNotPresent(String text)
    {
        TestCase.assertFalse(isNodePresent(text));
    }

    /**
     * Double click the text of the node in the browse tree that contains the given text
     *
     * @param text  the text of the selected browse tree node
     */
    public void doubleClickNode(String text)
    {
        selenium.doubleClick("//div[@id='browseTree']//a[text()='"+text+"']");
    }
}
