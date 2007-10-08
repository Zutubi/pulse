package com.zutubi.pulse.acceptance;

import com.thoughtworks.selenium.Selenium;
import com.thoughtworks.selenium.SeleniumException;
import com.zutubi.util.CollectionUtils;
import com.zutubi.util.Condition;
import com.zutubi.util.StringUtils;
import junit.framework.Assert;

/**
 * Utilities for working with selenium in test cases.
 */
public class SeleniumUtils
{
    public static final int DEFAULT_TIMEOUT = 30000;

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
        waitForElement(selenium, id, DEFAULT_TIMEOUT);
    }

    public static void waitForElement(Selenium selenium, String id, long timeout)
    {
        selenium.waitForCondition("selenium.browserbot.getCurrentWindow().document.getElementById('" + StringUtils.toValidHtmlName(id) + "') != null", Long.toString(timeout));
    }

    public static void refreshUntilElement(Selenium selenium, String id)
    {
        refreshUntilElement(selenium, id, DEFAULT_TIMEOUT);
    }

    public static void refreshUntilElement(final Selenium selenium, final String id, long timeout)
    {
        refreshUntil(selenium, timeout, new Condition()
        {
            public boolean satisfied()
            {
                return selenium.isElementPresent(StringUtils.toValidHtmlName(id));
            }
        }, "element '" + id + "'");
    }

    public static void refreshUntilText(final Selenium selenium, final String id, final String text)
    {
        refreshUntilText(selenium, id, text, DEFAULT_TIMEOUT);
    }

    public static void refreshUntilText(final Selenium selenium, final String id, final String text, long timeout)
    {
        refreshUntil(selenium, timeout, new Condition()
        {
            public boolean satisfied()
            {
                return text.equals(selenium.getText(StringUtils.toValidHtmlName(id)));
            }
        }, "text '" + text + "' in element '" + id + "'");
    }

    private static void refreshUntil(Selenium selenium, long timeout, Condition condition, String conditionText)
    {
        long startTime = System.currentTimeMillis();
        while(!condition.satisfied())
        {
            if(System.currentTimeMillis() - startTime > timeout)
            {
                throw new SeleniumException("Timed out after " + Long.toString(timeout) + "ms of waiting for " + conditionText);
            }

            try
            {
                Thread.sleep(1000);
            }
            catch (InterruptedException e)
            {
                throw new SeleniumException(e);
            }

            selenium.refresh();
            selenium.waitForPageToLoad("10000");
        }
    }

    public static void assertElementPresent(Selenium selenium, String id)
    {
        Assert.assertTrue("No element with id '" + id + "' found", selenium.isElementPresent(StringUtils.toValidHtmlName(id)));
    }

    public static void assertElementNotPresent(Selenium selenium, String id)
    {
        Assert.assertFalse("Unexpected element with id '" + id + "' found", selenium.isElementPresent(StringUtils.toValidHtmlName(id)));
    }

    public static void assertText(Selenium selenium, String id, String expectedText)
    {
        String actualText = selenium.getText(StringUtils.toValidHtmlName(id));
        Assert.assertEquals(expectedText, actualText);
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
