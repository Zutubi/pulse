package com.zutubi.pulse.acceptance;

import com.thoughtworks.selenium.DefaultSelenium;
import com.thoughtworks.selenium.Selenium;
import com.thoughtworks.selenium.SeleniumException;
import static com.zutubi.pulse.acceptance.AcceptanceTestUtils.getPulsePort;
import static com.zutubi.pulse.acceptance.AcceptanceTestUtils.getSeleniumBrowserProperty;
import com.zutubi.pulse.acceptance.forms.SeleniumForm;
import com.zutubi.pulse.acceptance.pages.SeleniumPage;
import com.zutubi.pulse.core.test.TestUtils;
import com.zutubi.pulse.master.webwork.Urls;
import com.zutubi.util.CollectionUtils;
import com.zutubi.util.Condition;
import com.zutubi.util.FileSystemUtils;
import com.zutubi.util.StringUtils;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Constructor;
import java.util.Arrays;

/**
 * A utility class for managing and interacting with the selenium instance.
 *
 * See {@link com.thoughtworks.selenium.Selenium} for details on the locator formats
 * available.
 */
public class SeleniumBrowser
{
    public static final long PAGELOAD_TIMEOUT = 30000;
    public static final long WAITFOR_TIMEOUT = 30000;
    public static final long REFRESH_TIMEOUT = 30000;

    private static final int SELENIUM_PORT = 4446;
    
    private Selenium selenium;
    private String browser;
    private boolean started = false;
    private String baseUrl;
    private Urls urls = Urls.getBaselessInstance();

    /**
     * Create a new instance of the selenium browser, using the browser and port
     * configured in the environment.  This constructor should be used by the
     * acceptance tests to ensure the correct configurations are used.
     */
    public SeleniumBrowser()
    {
        this(getPulsePort(), getSeleniumBrowserProperty());
    }

    public SeleniumBrowser(int port)
    {
        this(port, getSeleniumBrowserProperty());
    }

    public SeleniumBrowser(int port, String browser)
    {
        this.browser = browser;
        this.baseUrl = "http://localhost:" + port + "/";
        this.selenium = new DefaultSelenium("localhost", SELENIUM_PORT, browser, baseUrl);
    }

    public String getBaseUrl()
    {
        return baseUrl;
    }

    public Urls getUrls()
    {
        return urls;
    }

    /**
     * Start a fresh browser session by restarting the browser.
     */
    public synchronized void newSession()
    {
        stop();
        start();
    }

    /**
     * Start a browser if it is not already started.
     */
    public synchronized void start()
    {
        if (!started)
        {
            selenium.start();
            started = true;
        }
    }

    /**
     * Stop the browser if it has been started.
     */
    public synchronized void stop()
    {
        if (started)
        {
            selenium.stop();
            started = false;
        }
    }

    /**
     * Check if the native browser being driven by selenium is an
     * instance of firefox.
     *
     * @return true if it is firefox, false otherwise.
     */
    public boolean isFirefox()
    {
        return browser.contains("firefox");
    }

    /**
     * Create a new form instance, wiring it with the necessary resource.  The constructor
     * used is the constructor that matches provided arguments.
     *
     * @param formType      the type of the form being created.
     * @param extraArgs     the form types constructor arguments.
     * @param <T>           the form type T must extend {@link com.zutubi.pulse.acceptance.forms.SeleniumForm}
     * @return  a new instance of the form.
     */
    public <T extends SeleniumForm> T createForm(Class<T> formType, Object... extraArgs)
    {
        if (extraArgs == null)
        {
            extraArgs = new Object[0];
        }

        Object[] args = new Object[extraArgs.length + 1];
        args[0] = this;
        System.arraycopy(extraArgs, 0, args, 1, extraArgs.length);

        Class[] types = new Class[extraArgs.length + 1];
        types[0] = SeleniumBrowser.class;

        for (int i = 1; i < args.length; i++)
        {
            types[i] = args[i].getClass();
        }

        return createInstance(formType, types, args);
    }

    /**
     * Create a new page instance, wiring it with the necessary resource.  The constructor
     * used is the constructor that matches provided arguments.
     *
     * @param pageType      the type of the page being created.
     * @param extraArgs     the page types constructor arguments.
     * @param <T>           the page type T must extend {@link com.zutubi.pulse.acceptance.pages.SeleniumPage}
     * @return  a new instance of the form.
     */
    public <T extends SeleniumPage> T createPage(Class<T> pageType, Object... extraArgs)
    {
        if (extraArgs == null)
        {
            extraArgs = new Object[0];
        }

        Object[] args = new Object[extraArgs.length + 2];
        args[0] = this;
        args[1] = urls;
        System.arraycopy(extraArgs, 0, args, 2, extraArgs.length);

        Class[] types = new Class[extraArgs.length + 2];
        types[0] = SeleniumBrowser.class;
        types[1] = Urls.class;

        for (int i = 2; i < args.length; i++)
        {
            types[i] = args[i].getClass();
        }

        return createInstance(pageType, types, args);
    }

    /**
     * Create {@link #createPage(Class, Object[])} and open
     * {@link com.zutubi.pulse.acceptance.pages.SeleniumPage#open()} a new selenium page.
     *
     * @param pageType      the type of page being opened.
     * @param extraArgs     the page types constructor arguments.
     * @param <T>           the page type T must extend {@link com.zutubi.pulse.acceptance.pages.SeleniumPage}
     * @return  a new page instance that has been opened.
     */
    public <T extends SeleniumPage> T open(Class<T> pageType, Object... extraArgs)
    {
        T page = createPage(pageType, extraArgs);
        page.open();
        return page;
    }

    /**
     * Create {@link #createPage(Class, Object[])} and waitFor
     * {@link com.zutubi.pulse.acceptance.pages.SeleniumPage#waitFor()} a new selenium page.
     *
     * @param pageType      the type of page being opened.
     * @param extraArgs     the page types constructor arguments.
     * @param <T>           the page type T must extend {@link com.zutubi.pulse.acceptance.pages.SeleniumPage}
     * @return  a new page instance that has been waited for.
     */
    public <T extends SeleniumPage> T waitFor(Class<T> pageType, Object... extraArgs)
    {
        T page = createPage(pageType, extraArgs);
        page.waitFor();
        return page;
    }

    /**
     * Create {@link #createPage(Class, Object[])}, open {@link com.zutubi.pulse.acceptance.pages.SeleniumPage#open()}
     * and waitFor {@link com.zutubi.pulse.acceptance.pages.SeleniumPage#waitFor()} a new selenium page.
     *
     * @param pageType      the type of page being opened.
     * @param extraArgs     the page types constructor arguments.
     * @param <T>           the page type T must extend {@link com.zutubi.pulse.acceptance.pages.SeleniumPage}
     * @return  a new page instance that has been opened and waited for.
     */
    public <T extends SeleniumPage> T openAndWaitFor(Class<T> pageType, Object... extraArgs)
    {
        T page = createPage(pageType, extraArgs);
        page.open();
        page.waitFor();
        return page;
    }

    private <T> T createInstance(Class<T> type, Class[] types, Object[] args)
    {
        try
        {
            // map types to primitive equivalents.
            for (int i = 0; i < types.length; i++)
            {
                if (types[i] == Long.class)
                {
                    types[i] = Long.TYPE;
                }
                if (types[i] == Integer.class)
                {
                    types[i] = Integer.TYPE;
                }
                if (types[i] == Boolean.class)
                {
                    types[i] = Boolean.TYPE;
                }
            }

            Constructor<T> c = type.getConstructor(types);
            return c.newInstance(args);
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    /**
     * Open the selenium browser to the requested location.
     *
     * @param location  the location at which to open the browser, relative to the
     * base url.
     *
     * @see #getBaseUrl() 
     */
    public void open(String location)
    {
        selenium.open(StringUtils.join("/", true, baseUrl, location));
    }

    public void click(String locator)
    {
        selenium.click(locator);
    }

    public void doubleClick(String locator)
    {
        selenium.doubleClick(locator);
    }

    public void refresh()
    {
        selenium.refresh();
    }

    public void type(String id, String value)
    {
        selenium.type(id, value);
    }

    public void addSelection(String fieldLocator, String value)
    {
        selenium.addSelection(fieldLocator, value);
    }

    public void selectWindow(String title)
    {
        selenium.selectWindow(title);
    }

    /**
     * Verifies that the specified element is somewhere on the page.
     * @param id   id identifying the element
     * @return  true if the element is present, false otherwise.
     */
    public boolean isElementIdPresent(String id)
    {
        return selenium.isElementPresent(StringUtils.toValidHtmlName(id));
    }

    public boolean isElementPresent(String locator)
    {
        return selenium.isElementPresent(locator);
    }

    /**
     * Check if the specified text is present on the current page.
     * @param text  the required text
     * @return  true if the text is present, false otherwise.
     */
    public boolean isTextPresent(String text)
    {
        return selenium.isTextPresent(text);
    }

    /**
     * Check if a link with the specified id is present in the current page.
     * @param id    the link id
     * @return  true if the requested link is found, false otherwise
     */
    public boolean isLinkPresent(String id)
    {
        return CollectionUtils.contains(selenium.getAllLinks(), StringUtils.toValidHtmlName(id));
    }

    /**
     * Check if a link to the specified href is present in the current page.
     * @param href  the href / target of the link
     * @return  true if the requested link is found, false otherwise
     */
    public boolean isLinkToPresent(String href)
    {
        return selenium.isElementPresent("//a[@href='" + href + "']");
    }

    /**
     * Check if the specified element is visible.
     * @param locator   the locator identifying the element
     * @return  true if the element is visible, false otherwise.
     */
    public boolean isVisible(String locator)
    {
        return selenium.isVisible(locator);
    }

    public boolean isEditable(String fieldId)
    {
        return selenium.isEditable(fieldId);
    }

    public boolean isWindowPresent(String windowName)
    {
        return Arrays.asList(selenium.getAllWindowTitles()).contains(windowName);
    }

    public String evalVariable(String variable)
    {
        return selenium.getEval("selenium.browserbot.getCurrentWindow()." + variable);
    }

    public String evalExpression(String expression)
    {
        return selenium.getEval(expression);
    }

    public String getBodyText()
    {
        return selenium.getBodyText();
    }

    public String getText(String locator)
    {
        return selenium.getText(locator);
    }

    public String[] getAllWindowTitles()
    {
        return selenium.getAllWindowTitles();
    }

    public String getAttribute(String locator)
    {
        return selenium.getAttribute(locator);
    }

    public String[] getAllLinks()
    {
        return selenium.getAllLinks();
    }

    public String getCellContents(String tableLocator, int row, int column)
    {
        return selenium.getTable(StringUtils.toValidHtmlName(tableLocator + "." + row + "." + column));
    }

    public String[] getSelectOptions(String fieldId)
    {
        return selenium.getSelectOptions(fieldId);
    }

    public String getValue(String fieldId)
    {
        return selenium.getValue(fieldId);
    }

    public String getTitle()
    {
        return selenium.getTitle();
    }

    public void waitForPageToLoad()
    {
        waitForPageToLoad(PAGELOAD_TIMEOUT);
    }

    public void waitForPageToLoad(long timeout)
    {
        selenium.waitForPageToLoad(Long.toString(timeout));
    }

    public void waitForVariable(String variable)
    {
        waitForVariable(variable, false);
    }

    public void waitForVariable(String variable, boolean inverse)
    {
        waitForCondition((inverse ? "!" : "") + "selenium.browserbot.getCurrentWindow()." + variable, WAITFOR_TIMEOUT);
    }

    public void waitAndClick(String id)
    {
        waitForElement(id);
        click(id);
    }

    public void waitForElement(String id)
    {
        waitForElement(id, WAITFOR_TIMEOUT);
    }

    public void waitForElement(String id, long timeout)
    {
        waitForCondition("selenium.browserbot.findElementOrNull('id=" + StringUtils.toValidHtmlName(id) + "') != null", timeout);
    }

    public void waitForLocator(String locator)
    {
        waitForLocator(locator, false);
    }
    
    public void waitForLocator(String locator, boolean invert)
    {
        long startTime = System.currentTimeMillis();
        while (System.currentTimeMillis() - startTime < WAITFOR_TIMEOUT)
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

        File failureFile = captureFailure();
        throw new SeleniumException("Timeout out after " + WAITFOR_TIMEOUT + "ms (see: " + failureFile.getName() + ")");
    }

    public void waitForCondition(String condition)
    {
        waitForCondition(condition, WAITFOR_TIMEOUT);
    }

    public void waitForCondition(String condition, long timeout)
    {
        try
        {
            selenium.waitForCondition(condition, Long.toString(timeout));
        }
        catch (SeleniumException e)
        {
            File failureFile = captureFailure();
            throw new RuntimeException("Selenium timeout (see: " + failureFile.getName() + "): " + e.getMessage(), e);
        }
    }

    public void waitForVisible(final String locator)
    {
        try
        {
            TestUtils.waitForCondition(new Condition()
            {
                public boolean satisfied()
                {
                    return selenium.isVisible(locator);
                }
            }, WAITFOR_TIMEOUT, "locator '" + locator + "' to become visible");
        }
        catch (Exception e)
        {
            File failureFile = captureFailure();
            throw new RuntimeException(e.getMessage() + " (see: " + failureFile.getName() + ")", e);
        }
    }

    public void waitForWindow(final String windowName)
    {
        TestUtils.waitForCondition(new Condition()
        {
            public boolean satisfied()
            {
                return isWindowPresent(windowName);
            }
        }, WAITFOR_TIMEOUT, "Timeout waiting for " + windowName + " window.");
    }

    public void refreshUntilElement(String id)
    {
        refreshUntilElement(id, REFRESH_TIMEOUT);
    }

    public void refreshUntilElement(final String id, long timeout)
    {
        refreshUntil(timeout, new Condition()
        {
            public boolean satisfied()
            {
                return selenium.isElementPresent(StringUtils.toValidHtmlName(id));
            }
        }, "element '" + id + "'");
    }

    public void refreshUntilText(final String id, final String text)
    {
        refreshUntilText(id, text, REFRESH_TIMEOUT);
    }

    public void refreshUntilText(final String id, final String text, long timeout)
    {
        refreshUntil(timeout, new Condition()
        {
            public boolean satisfied()
            {
                return text.equals(selenium.getText(StringUtils.toValidHtmlName(id)));
            }
        }, "text '" + text + "' in element '" + id + "'");
    }

    public void refreshUntil(long timeout, Condition condition, String conditionText)
    {
        long startTime = System.currentTimeMillis();
        while (!condition.satisfied())
        {
            if (System.currentTimeMillis() - startTime > timeout)
            {
                File failureFile = captureFailure();
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

    private File captureFailure()
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
}