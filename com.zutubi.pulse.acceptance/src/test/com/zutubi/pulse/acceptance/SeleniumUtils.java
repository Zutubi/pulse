package com.zutubi.pulse.acceptance;

import com.thoughtworks.selenium.Selenium;
import com.thoughtworks.selenium.SeleniumException;
import com.zutubi.util.CollectionUtils;
import com.zutubi.util.Condition;
import com.zutubi.util.StringUtils;
import com.zutubi.util.SystemUtils;
import com.zutubi.util.logging.Logger;
import junit.framework.Assert;

/**
 * Utilities for working with selenium in test cases.
 */
public class SeleniumUtils
{
    private static final Logger LOG = Logger.getLogger(SeleniumUtils.class);

    public static final int DEFAULT_TIMEOUT = 30000;

    public static void waitForVariable(Selenium selenium, String variable, long timeout)
    {
        waitForVariable(selenium, variable, timeout, false);
    }
    
    public static void waitForVariable(Selenium selenium, String variable, long timeout, boolean inverse)
    {
        selenium.waitForCondition((inverse ? "!" : "") + "selenium.browserbot.getCurrentWindow()." + variable, Long.toString(timeout));
    }

    public static String evalVariable(Selenium selenium, String variable)
    {
        return selenium.getEval("selenium.browserbot.getCurrentWindow()." + variable);
    }

    public static void waitForElementId(Selenium selenium, String id)
    {
        waitForElementId(selenium, id, DEFAULT_TIMEOUT);
    }

    public static void waitForElementId(Selenium selenium, String id, long timeout)
    {
        selenium.waitForCondition("selenium.browserbot.findElementOrNull('id=" + StringUtils.toValidHtmlName(id) + "') != null", Long.toString(timeout));
    }

    public static void waitForLocator(Selenium selenium, String locator)
    {
        waitForLocator(selenium, locator, DEFAULT_TIMEOUT, false);
    }

    public static void waitForLocator(Selenium selenium, String locator, long timeout, boolean invert)
    {
        long startTime = System.currentTimeMillis();
        while(System.currentTimeMillis() - startTime < timeout)
        {
            boolean present = selenium.isElementPresent(locator);
            if(present && !invert || !present && invert)
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
        while (!condition.satisfied())
        {
            if (System.currentTimeMillis() - startTime > timeout)
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
            try
            {
                selenium.waitForPageToLoad("10000");
            }
            catch (Exception e)
            {
                LOG.warning(e);
            }
        }
    }

    public static void waitForVisible(final Selenium selenium, final String locator)
    {
        awaitCondition(DEFAULT_TIMEOUT, new Condition()
        {
            public boolean satisfied()
            {
                return selenium.isVisible(locator);
            }
        }, "locator '" + locator + "' to become visible");
    }

    private static void awaitCondition(long timeout, Condition condition, String conditionText)
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
        }
    }

    public static void waitAndClickId(Selenium selenium, String id)
    {
        waitForElementId(selenium, id);
        selenium.click(id);
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

    public static void assertTextNotPresent(Selenium selenium, String text)
    {
        Assert.assertFalse(selenium.isTextPresent(text));
    }

    public static boolean isLinkPresent(Selenium selenium, String id)
    {
        return CollectionUtils.contains(selenium.getAllLinks(), StringUtils.toValidHtmlName(id));
    }

    public static void assertLinkPresent(Selenium selenium, String id)
    {
        Assert.assertTrue(isLinkPresent(selenium, id));
    }

    public static void assertLinkNotPresent(Selenium selenium, String id)
    {
        Assert.assertFalse(isLinkPresent(selenium, id));
    }

    public static void assertFormFieldNotEmpty(Selenium selenium, String id)
    {
        String value = selenium.getValue(StringUtils.toValidHtmlName(id));
        Assert.assertNotNull(value);
        Assert.assertTrue(value.length() > 0);
    }

    public static void assertVisible(Selenium selenium, String locator)
    {
        Assert.assertTrue(selenium.isVisible(locator));
    }

    public static void assertNotVisible(Selenium selenium, String locator)
    {
        Assert.assertFalse(selenium.isVisible(locator));
    }

    public static String getCellContents(Selenium selenium, String tableLocator, int row, int column)
    {
        return selenium.getTable(StringUtils.toValidHtmlName(tableLocator + "." + row + "." + column));
    }

    public static void assertCellContents(Selenium selenium, String tableLocator, int row, int column, String text)
    {
        Assert.assertEquals(text, getCellContents(selenium, tableLocator, row, column));
    }

    public static String getSeleniumBrowserProperty()
    {
        String browser = System.getenv("SELENIUM_BROWSER");
        if (browser == null)
        {
            if (SystemUtils.IS_WINDOWS)
            {
                browser = "*iexplore";
            }
            else
            {
                browser = "*firefox";
            }
        }
        return browser;        
    }

    public static boolean isLinkToPresent(Selenium selenium, String href)
    {
        return selenium.isElementPresent("//a[@href='" + href + "']");
    }

    public static void assertLinkToPresent(Selenium selenium, String href)
    {
        Assert.assertTrue("Link to '" + href + "' not found", isLinkToPresent(selenium, href));
    }

    public static void assertLinkToNotPresent(Selenium selenium, String href)
    {
        Assert.assertFalse("Unexpected link to '" + href + "' found", isLinkToPresent(selenium, href));
    }
}
