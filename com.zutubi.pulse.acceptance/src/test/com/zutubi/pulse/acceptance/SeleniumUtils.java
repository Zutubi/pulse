package com.zutubi.pulse.acceptance;

import com.thoughtworks.selenium.Selenium;
import com.thoughtworks.selenium.SeleniumException;
import com.zutubi.pulse.core.test.TestUtils;
import com.zutubi.util.*;
import junit.framework.Assert;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * Utilities for working with selenium in test cases.
 */
public class SeleniumUtils
{
    public static final int DEFAULT_TIMEOUT = 30000;

    public static File captureFailure(Selenium selenium)
    {
        int i = 1;
        File failureFile;
        do
        {
            failureFile = new File("working", "failure-" + i + ".txt");
            i++;
        }
        while(failureFile.exists());

        try
        {
            String text;
            try
            {
                text = selenium.getBodyText();
            }
            catch (Exception e)
            {
                StringWriter stringWriter = new StringWriter();
                PrintWriter printWriter = new PrintWriter(stringWriter);

                printWriter.println("Unable to get body text using selenium:");
                e.printStackTrace(printWriter);
                text = stringWriter.toString();
            }

            FileSystemUtils.createFile(failureFile, text);
        }
        catch (IOException e)
        {
            // You have to be kidding me.
            throw new RuntimeException(e);
        }

        return failureFile;
    }

    public static void waitForCondition(Selenium selenium, String condition, long timeout)
    {
        try
        {
            selenium.waitForCondition(condition, Long.toString(timeout));
        }
        catch (SeleniumException e)
        {
            File failureFile = captureFailure(selenium);
            throw new RuntimeException("Selenium timeout (see: " + failureFile.getName() + "): " + e.getMessage(), e);
        }
    }

    public static void waitForVariable(Selenium selenium, String variable, long timeout)
    {
        waitForVariable(selenium, variable, timeout, false);
    }

    public static void waitForVariable(Selenium selenium, String variable, long timeout, boolean inverse)
    {
        waitForCondition(selenium, (inverse ? "!" : "") + "selenium.browserbot.getCurrentWindow()." + variable, timeout);
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
        waitForCondition(selenium, "selenium.browserbot.findElementOrNull('id=" + StringUtils.toValidHtmlName(id) + "') != null", timeout);
    }

    public static void waitForLocator(Selenium selenium, String locator)
    {
        waitForLocator(selenium, locator, DEFAULT_TIMEOUT, false);
    }

    public static void waitForLocator(Selenium selenium, String locator, long timeout, boolean invert)
    {
        long startTime = System.currentTimeMillis();
        while (System.currentTimeMillis() - startTime < timeout)
        {
            boolean present = selenium.isElementPresent(locator);
            if (present && !invert || !present && invert)
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

        File failureFile = captureFailure(selenium);
        throw new SeleniumException("Timeout out after " + timeout + "ms (see: " + failureFile.getName() + ")");
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
                String safeId = StringUtils.toValidHtmlName(id);
                waitForElementId(selenium, safeId);
                return text.equals(selenium.getText(safeId));
            }
        }, "text '" + text + "' in element '" + id + "'");
    }

    public static void refreshUntil(Selenium selenium, long timeout, Condition condition, String conditionText)
    {
        long startTime = System.currentTimeMillis();
        while (!condition.satisfied())
        {
            if (System.currentTimeMillis() - startTime > timeout)
            {
                File failureFile = captureFailure(selenium);
                throw new SeleniumException("Timed out after " + Long.toString(timeout) + "ms of waiting for " + conditionText + " (see: " + failureFile.getName() + ")");
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
                e.printStackTrace(System.err);
            }
        }
    }

    public static void waitForVisible(final Selenium selenium, final String locator)
    {
        try
        {
            TestUtils.waitForCondition(new Condition()
            {
                public boolean satisfied()
                {
                    return selenium.isVisible(locator);
                }
            }, DEFAULT_TIMEOUT, "locator '" + locator + "' to become visible");
        }
        catch (Exception e)
        {
            File failureFile = captureFailure(selenium);
            throw new RuntimeException(e.getMessage() + " (see: " + failureFile.getName() + ")", e);
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
                browser = "*iexploreproxy";
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
