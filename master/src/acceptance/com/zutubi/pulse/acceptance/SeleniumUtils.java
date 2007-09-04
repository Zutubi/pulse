package com.zutubi.pulse.acceptance;

import com.thoughtworks.selenium.Selenium;
import com.zutubi.util.CollectionUtils;
import com.zutubi.util.StringUtils;
import junit.framework.Assert;

/**
 * Utilities for working with selenium in test cases.
 */
public class SeleniumUtils
{
    public static void waitForVariable(Selenium selenium, String variable, long timeout)
    {
        selenium.waitForCondition("selenium.browserbot.getCurrentWindow()." + variable, Long.toString(timeout));
    }

    public static String evalVariable(Selenium selenium, String variable)
    {
        return selenium.getEval("selenium.browserbot.getCurrentWindow()." + variable);       
    }

    public static void waitForElement(Selenium selenium, String id)
    {
        waitForElement(selenium, id, 30000);
    }

    public static void waitForElement(Selenium selenium, String id, long timeout)
    {
        selenium.waitForCondition("selenium.browserbot.getCurrentWindow().document.getElementById('" + StringUtils.toValidHtmlName(id) + "') != null", Long.toString(timeout));
    }

    public static void assertElementPresent(Selenium selenium, String id)
    {
        Assert.assertTrue("No element with id '" + id + "' found", selenium.isElementPresent(StringUtils.toValidHtmlName(id)));
    }

    public static void assertElementNotPresent(Selenium selenium, String id)
    {
        Assert.assertFalse("Unexpected element with id '" + id + "' found", selenium.isElementPresent(StringUtils.toValidHtmlName(id)));
    }

    public static void assertTextPresent(Selenium selenium, String text)
    {
        Assert.assertTrue(selenium.isTextPresent(text));
    }

    public static void assertLinkPresent(Selenium selenium, String id)
    {
        Assert.assertTrue(CollectionUtils.contains(selenium.getAllLinks(), StringUtils.toValidHtmlName(id)));
    }

    public static void assertLinkNotPresent(Selenium selenium, String id)
    {
        Assert.assertFalse(CollectionUtils.contains(selenium.getAllLinks(), StringUtils.toValidHtmlName(id)));
    }

    public static void assertFormFieldNotEmpty(Selenium selenium, String id)
    {
        String value = selenium.getValue(StringUtils.toValidHtmlName(id));
        Assert.assertNotNull(value);
        Assert.assertTrue(value.length() > 0);
    }
}
