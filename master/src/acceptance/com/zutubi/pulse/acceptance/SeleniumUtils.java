package com.zutubi.pulse.acceptance;

import com.thoughtworks.selenium.Selenium;

/**
 * Utilities for working with selenium in test cases.
 */
public class SeleniumUtils
{
    public static void waitForElement(Selenium selenium, String id)
    {
        waitForElement(selenium, id, 30000);
    }

    public static void waitForElement(Selenium selenium, String id, long timeout)
    {
        selenium.waitForCondition("selenium.browserbot.getCurrentWindow().document.getElementById('" + id + "') != null", Long.toString(timeout));
    }
}
