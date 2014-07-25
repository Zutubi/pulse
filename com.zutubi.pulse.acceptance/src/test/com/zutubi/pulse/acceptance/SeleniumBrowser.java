package com.zutubi.pulse.acceptance;

import com.google.common.io.Files;
import com.thoughtworks.selenium.Selenium;
import com.thoughtworks.selenium.SeleniumException;
import com.zutubi.pulse.acceptance.forms.LoginForm;
import com.zutubi.pulse.acceptance.forms.SeleniumForm;
import com.zutubi.pulse.acceptance.pages.LoginPage;
import com.zutubi.pulse.acceptance.pages.SeleniumPage;
import com.zutubi.pulse.core.test.TestUtils;
import com.zutubi.pulse.core.test.TimeoutException;
import com.zutubi.pulse.master.webwork.Urls;
import com.zutubi.util.Condition;
import com.zutubi.util.StringUtils;
import com.zutubi.util.SystemUtils;
import com.zutubi.util.WebUtils;
import freemarker.template.utility.StringUtil;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Wait;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Constructor;
import java.nio.charset.Charset;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.zutubi.pulse.acceptance.AcceptanceTestUtils.ADMIN_CREDENTIALS;
import static com.zutubi.pulse.acceptance.AcceptanceTestUtils.getWorkingDirectory;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;

/**
 * A utility class for managing and interacting with the selenium instance.
 *
 * See {@link com.thoughtworks.selenium.Selenium} for details on the locator formats
 * available.
 */
public class SeleniumBrowser
{
    public static final long DEFAULT_TIMEOUT = 60000;
    public static final long WAITFOR_TIMEOUT = 60000;
    public static final long REFRESH_TIMEOUT = 60000;

    public static final long WAITFOR_INTERVAL = 3000;
    public static final long REFRESH_INTERVAL = 1000;

    private Selenium selenium;
    private int pulsePort;
    private WebDriver webDriver;
    private Urls urls;

    private static WebDriver createWebDriver()
    {
        if (SystemUtils.IS_WINDOWS)
        {
            return new InternetExplorerDriver();
        }
        else
        {
            FirefoxProfile profile = new FirefoxProfile();
            String logFile = System.getProperty("selenium.firefox.log");
            if (logFile != null)
            {
                profile.setPreference("webdriver.log.file", logFile);
            }
         
            profile.setEnableNativeEvents(true);
            return new FirefoxDriver(profile);
        }
    }

    /**
     * Create a new instance of the selenium browser, using the browser and port
     * configured in the environment.  This constructor should be used by the
     * acceptance tests to ensure the correct configurations are used.
     */
    public SeleniumBrowser()
    {
        this(AcceptanceTestUtils.getPulsePort(), createWebDriver());
    }

    public SeleniumBrowser(int port)
    {
        this(port, createWebDriver());
    }

    public SeleniumBrowser(int port, WebDriver webDriver)
    {
        this.webDriver = webDriver;
        webDriver.manage().timeouts().implicitlyWait(1, TimeUnit.SECONDS);
        String baseUrl = AcceptanceTestUtils.getPulseUrl(port);
        selenium = new WebDriverBackedSelenium(webDriver, baseUrl);
        this.pulsePort = port;
        urls = new Urls(baseUrl);
    }

    /**
     * Start a fresh browser session by restarting the browser.
     */
    public synchronized void newSession()
    {
        webDriver.manage().deleteAllCookies();
        webDriver.get(urls.base());
    }

    public void quit()
    {
        webDriver.quit();
    }
    
    /**
     * Check if the native browser being driven by selenium is an
     * instance of firefox.
     *
     * @return true if it is firefox, false otherwise.
     */
    public boolean isFirefox()
    {
        return webDriver instanceof FirefoxDriver;
    }

    /**
     * Returns the Pulse port the browser is connecting to.
     * 
     * @return the Pulse port to connect to
     */
    public int getPulsePort()
    {
        return pulsePort;
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
     * Create {@link #createPage(Class, Object...)} and open
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
     * Create {@link #createPage(Class, Object...)} and waitFor
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
     * Create {@link #createPage(Class, Object...)} open {@link com.zutubi.pulse.acceptance.pages.SeleniumPage#open()}
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
     * Goes to the login page and logs in with the given credentials.
     *
     * @param username user to log in as
     * @param password the given user's password
     */
    public void login(String username, String password)
    {
        LoginPage page = openAndWaitFor(LoginPage.class);
        page.login(username, password);
    }

    /**
     * Goes to the login page, logs in with the given credentials and waits for the login to succeed.
     *
     * @param username user to log in as
     * @param password the given user's password
     */
    public void loginAndWait(String username, String password)
    {
        LoginPage page = openAndWaitFor(LoginPage.class);
        page.login(username, password);
        waitForElement(By.id(IDs.ID_LOGOUT));
    }

    /**
     * Goes to the login page and logs in as the admin user.
     */
    public void loginAsAdmin()
    {
        login(ADMIN_CREDENTIALS.getUserName(), ADMIN_CREDENTIALS.getPassword());
    }

    /**
     * Click the logout link on the browser and wait for the page to load. This
     * assumes that the logout link is available.
     */
    public void logout()
    {
        if (!isLoggedIn())
        {
            throw new IllegalStateException("Can not logout when no logout link is available.");
        }
        waitAndClick(By.id(IDs.ID_LOGOUT));
        createForm(LoginForm.class).waitFor();
    }

    /**
     * @return true if the web browser session has an active login
     */
    public boolean isLoggedIn()
    {
        return isElementIdPresent(IDs.ID_LOGOUT);
    }

    /**
     * Open the selenium browser to the requested location.
     *
     * @param location  the location at which to open the browser.
     */
    public void open(String location)
    {
        webDriver.navigate().to(location);
    }

    /**
     * Clicks on an element.  Respects the implicit wait period.
     * 
     * @param by element locator
     */
    public void click(By by)
    {
        webDriver.findElement(by).click();
    }

    /**
     * Double clicks on an element.  Respects the implicit wait period.
     * 
     * @param by element locator
     */
    public void doubleClick(By by)
    {
        new Actions(webDriver).doubleClick(webDriver.findElement(by)).build().perform();
    }

    /**
     * Refreshes the current web page.
     */
    public void refresh()
    {
        webDriver.navigate().refresh();
    }

    /**
     * Types the given value into a text field or area.  Any existing text is
     * cleared first.
     * 
     * @param by    locator of the text field/area
     * @param value the text to type
     */
    public void type(By by, String value)
    {
        WebElement element = webDriver.findElement(by);
        element.clear();
        element.sendKeys(value);
    }

    /**
     * Adds a selection to the current value of a multi-value element.
     * 
     * @param fieldId identifier of the field
     * @param value   value to add
     */
    public void addSelection(String fieldId, String value)
    {
        selenium.addSelection(fieldId, value);
    }

    /**
     * Checks if the specified element is on the current page.  Respects the
     * implicit wait period.
     * 
     * @param id id identifying the element
     * @return true if the element is present, false otherwise.
     */
    public boolean isElementIdPresent(String id)
    {
        return isElementPresent(By.id(WebUtils.toValidHtmlName(id)));
    }

    /**
     * Checks if the specified element is on the current page.  Respects the 
     * implicit wait period.
     * 
     * @param by locator of the element to check for
     * @return true iff the given element is present (it need not be visible)
     */
    public boolean isElementPresent(By by)
    {
        return webDriver.findElements(by).size() > 0;
    }

    /**
     * Check if the specified text is present on the current page.
     * 
     * @param text the required text
     * @return true if the text is present, false otherwise.
     */
    public boolean isTextPresent(String text)
    {
        return selenium.isTextPresent(text);
    }

    public void waitForTextPresent(final String text)
    {
        Wait<WebDriver> wait = new WebDriverWait(webDriver, WAITFOR_TIMEOUT/1000);
        wait.until(new ExpectedCondition<Boolean>()
        {
            public Boolean apply(WebDriver webDriver)
            {
                return selenium.isTextPresent(text);
            }
        });
    }

    /**
     * Check if the text matching the regex is present on the current page.
     * 
     * @param regex the regex
     * @return true if a match is present, false otherwise.
     */
    public boolean isRegexPresent(String regex)
    {
        String bodyText = selenium.getBodyText();

        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(bodyText);
        return m.find();
    }

    /**
     * Check if a link with the specified id is present in the current page.
     * 
     * @param id the link id
     * @return true if the requested link is found, false otherwise
     */
    public boolean isLinkPresent(String id)
    {
        return isElementPresent(By.linkText(WebUtils.toValidHtmlName(id)));
    }

    /**
     * Check if a link to the specified href is present in the current page.
     * 
     * @param href the href / target of the link
     * @return true if the requested link is found, false otherwise
     */
    public boolean isLinkToPresent(String href)
    {
        return isElementPresent(By.xpath("//a[@href=\"" + href + "\"]"));
    }
    
    /**
     * Check if the specified element is visible.
     * 
     * @param by the method by which to identify the element
     * @return  true if the element is visible, false otherwise.
     */
    public boolean isVisible(By by)
    {
        try
        {
            return webDriver.findElement(by).isDisplayed();
        }
        catch (NoSuchElementException e)
        {
            return false;
        }
    }

    public boolean isEditable(String fieldId)
    {
        return selenium.isEditable(fieldId);
    }

    public Object evaluateScript(String expression)
    {
        return ((JavascriptExecutor) webDriver).executeScript(expression);
    }

    public String getBodyText()
    {
        return selenium.getBodyText();
    }

    public String getText(By by)
    {
        WebElement element = webDriver.findElement(by);
        return element.getText().trim();
    }

    public String getAttribute(By by, String attribute)
    {
        return webDriver.findElement(by).getAttribute(attribute);
    }

    public String[] getAllLinks()
    {
        return selenium.getAllLinks();
    }

    public String getCellContents(String tableId, int row, int column)
    {
        return selenium.getTable(WebUtils.toValidHtmlName(tableId) + "." + row + "." + column);
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

    public boolean isCookiePresent(String name)
    {
        return selenium.isCookiePresent(name);
    }

    public void deleteAllCookies()
    {
        selenium.deleteAllVisibleCookies();
    }

    public void waitForVariable(String variable)
    {
        waitForVariable(variable, false);
    }

    public void waitForVariable(final String variable, final boolean inverse)
    {
        Wait<WebDriver> wait = new WebDriverWait(webDriver, WAITFOR_TIMEOUT/1000);
        wait.until(new ExpectedCondition<Object>()
        {
            public Object apply(WebDriver webDriver)
            {
                JavascriptExecutor executor = (JavascriptExecutor) webDriver;
                return executor.executeScript("var r = " + variable + " !== undefined && " +
                                                           variable + " !== null && " +
                                                           variable + " !== false; return " + (inverse ? "!" : "") + "r");
            }
        });
    }

    public WebElement waitForElement(String id)
    {
        return waitForElement(id, WAITFOR_TIMEOUT);
    }

    public WebElement waitForElement(final String id, long timeout)
    {
        return waitForElement(By.id(id), timeout);
    }

    public WebElement waitForElement(By by)
    {
        return waitForElement(by, WAITFOR_TIMEOUT);
    }

    public WebElement waitForElement(final By by, long timeout)
    {
        Wait<WebDriver> wait = new WebDriverWait(webDriver, timeout/1000, 250).ignoring(RuntimeException.class);
        return wait.until(ExpectedConditions.presenceOfElementLocated(by));
    }

    public void waitAndClick(By by)
    {
        Wait<WebDriver> wait = new WebDriverWait(webDriver, WAITFOR_TIMEOUT /1000, 250).ignoring(RuntimeException.class);
        wait.until(ExpectedConditions.elementToBeClickable(by));
        click(by);
    }

    public void waitForElementToDisappear(final By by)
    {
        Wait<WebDriver> wait = new WebDriverWait(webDriver, WAITFOR_TIMEOUT/1000, WAITFOR_INTERVAL).ignoring(RuntimeException.class);
        wait.until(ExpectedConditions.invisibilityOfElementLocated(by));
    }

    public void waitForCondition(String condition)
    {
        waitForCondition(condition, WAITFOR_TIMEOUT);
    }

    public void waitForCondition(final String condition, long timeout)
    {
        Wait<WebDriver> wait = new WebDriverWait(webDriver, timeout/1000);
        wait.until(new ExpectedCondition<Object>()
        {
            public Object apply(WebDriver webDriver)
            {
                return ((JavascriptExecutor) webDriver).executeScript(condition);
            }
        });
    }

    public void waitForVisible(final String locator)
    {
        TestUtils.waitForCondition(new Condition()
        {
            public boolean satisfied()
            {
                return selenium.isVisible(locator);
            }
        }, WAITFOR_TIMEOUT, "locator '" + locator + "' to become visible.");
    }

    /**
     * Waits for the pop-down status pane to appear with the given message.
     *
     * @param message message to wait for
     */
    public void waitForStatus(String message)
    {
        waitForElement(IDs.STATUS_MESSAGE, WAITFOR_TIMEOUT);
        TestUtils.waitForCondition(new Condition()
        {
            public boolean satisfied()
            {
                return StringUtils.stringSet(getText(By.id(IDs.STATUS_MESSAGE)));
            }
        }, WAITFOR_TIMEOUT, "status message to be set.");

        String text = getText(By.id(IDs.STATUS_MESSAGE));
        assertThat(text, containsString(message));
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
                return selenium.isElementPresent(WebUtils.toValidHtmlName(id));
            }
        }, "element '" + id + "'");
    }

    public void refreshUntil(long timeout, Condition condition, String conditionText)
    {
        long endTime = System.currentTimeMillis() + timeout;
        
        while (!condition.satisfied())
        {
            long remainingTime = endTime - System.currentTimeMillis();
            if (remainingTime <= 0)
            {
                throw new TimeoutException("Timed out after " + Long.toString(timeout) + "ms of waiting for " + conditionText);
            }

            try
            {
                Thread.sleep(REFRESH_INTERVAL);
            }
            catch (InterruptedException e)
            {
                throw new SeleniumException(e);
            }

            selenium.refresh();
            try
            {
                selenium.waitForPageToLoad(String.valueOf(remainingTime));
            }
            catch (Exception e)
            {
                e.printStackTrace(System.err);
            }
        }
    }

    public void captureFailure(String testName)
    {
        try
        {
            captureBodyText(testName);
            captureScreenshot(testName);
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }

    private void captureBodyText(String testName) throws IOException
    {
        String text;
        try
        {
            text = selenium.getHtmlSource();
        }
        catch (Exception e)
        {
            text = stackTraceAsString(e, "Unable to get HTML source using selenium:");
        }

        Files.write(text, new File(getWorkingDirectory(), testName + "-failure.html"), Charset.defaultCharset());
    }

    private void captureScreenshot(String testName) throws IOException
    {
        String screenshotFilename = new File(getWorkingDirectory(), testName + "-failure.png").getAbsolutePath();
        try
        {
            selenium.captureScreenshot(screenshotFilename);
        }
        catch (Exception e)
        {
            Files.write(stackTraceAsString(e, "Unable to capture screenshot with selenium:"), new File(getWorkingDirectory(), testName + "-failure.png.txt"), Charset.defaultCharset());
        }
    }

    private String stackTraceAsString(Exception e, String prelude)
    {
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        printWriter.println(prelude);
        e.printStackTrace(printWriter);
        return stringWriter.toString();
    }

    /**
     * Sets the value of an Ext combo box with a given component id.
     *
     * @param comboId component id of the combo
     * @param value   value to set the combo to
     */
    public void setComboByValue(String comboId, String value)
    {
        String indexExpression;
        // Annoyingly ext stores can't find the empty string value...
        if (StringUtils.stringSet(value))
        {
            indexExpression = "store.find(store.fields.first().name, '" + StringUtil.javaScriptStringEnc(value) + "')";
        }
        else
        {
            indexExpression = "0";
        }

        evaluateScript(
                "var combo = Ext.getCmp('" + comboId + "');" +
                        "combo.setValue('" + StringUtil.javaScriptStringEnc(value) + "');" +
                        "var store = combo.getStore();" +
                        "combo.fireEvent('select', combo, store.getAt(" + indexExpression + "));"
        );
    }

    /**
     * Retrieves the current value of the Ext combo box with the given
     * component id.
     * 
     * @param comboId component id of the combo
     * @return current value of the combo
     */
    public String getComboValue(String comboId)
    {
        return (String) evaluateScript("return Ext.getCmp('" + comboId + "').getValue()");
    }

    /**
     * Retrieves the available options in the Ext combo box with the given
     * component id.
     * 
     * @param comboId component id of the combo
     * @return available options in the combo
     */
    @SuppressWarnings("unchecked")
    public List<String> getComboOptions(String comboId)
    {
        String js = "return function() { " +
                        "var combo = Ext.getCmp('" + comboId + "'); " +
                        "var values = []; " +
                        "combo.store.each(function(r) { values.push(r.get(combo.valueField)); }); " +
                        "return values; " +
                    "}();";
        return (List<String>) evaluateScript(js);
    }

    /**
     * Retrieves the displayed strings for available options in the Ext combo
     * box with the given component id.
     *
     * @param comboId component id of the combo
     * @return displayed strings for available options in the combo
     */
    @SuppressWarnings("unchecked")
    public List<String> getComboDisplays(String comboId)
    {
        String js = "return function() { " +
                        "var combo = Ext.getCmp('" + comboId + "'); " +
                        "var values = []; " +
                        "combo.store.each(function(r) { values.push(r.get(combo.displayField)); }); " +
                        "return values; " +
                    "}();";
        return (List<String>) evaluateScript(js);
    }
}
