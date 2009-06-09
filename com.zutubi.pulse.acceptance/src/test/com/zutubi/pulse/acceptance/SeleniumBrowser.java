package com.zutubi.pulse.acceptance;

import com.thoughtworks.selenium.DefaultSelenium;
import com.thoughtworks.selenium.Selenium;
import com.thoughtworks.selenium.SeleniumException;
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

/**
 * A utility class for managing and interacting with the selenium instance.
 */
public class SeleniumBrowser
{
    public static final long PAGELOAD_TIMEOUT = 30000;
    public static final long WAITFOR_TIMEOUT = 30000;

    private static final int SELENIUM_PORT = 4446;
    
    private Selenium selenium;
    private boolean started = false;
    private String baseUrl;
    private Urls urls = Urls.getBaselessInstance();

    public SeleniumBrowser()
    {
        //todo: SeleniumUtils.getSeleniumBrowserProperty() also uses a env property.
        this(AcceptanceTestUtils.getPulsePort(), SeleniumUtils.getSeleniumBrowserProperty());
    }

    public SeleniumBrowser(int port)
    {
        this(port, SeleniumUtils.getSeleniumBrowserProperty());
    }

    public SeleniumBrowser(int port, String browser)
    {
        baseUrl = "http://localhost:" + port + "/";
        selenium = new DefaultSelenium("localhost", SELENIUM_PORT, browser, baseUrl);
    }

    public String getBaseUrl()
    {
        return baseUrl;
    }

    public void newSession()
    {
        stop();
        start();
    }

    public void start()
    {
        if (!started)
        {
            selenium.start();
            started = true;
        }
    }

    public void stop()
    {
        if (started)
        {
            selenium.stop();
            started = false;
        }
    }

    public Selenium getSelenium()
    {
        return selenium;
    }

    public <T extends SeleniumForm> T createForm(Class<T> formType, Object... extraArgs)
    {
        if (extraArgs == null)
        {
            extraArgs = new Object[0];
        }

        Object[] args = new Object[extraArgs.length + 1];
        args[0] = selenium;
        System.arraycopy(extraArgs, 0, args, 1, extraArgs.length);

        Class[] types = new Class[extraArgs.length + 1];
        types[0] = Selenium.class;

        for (int i = 1; i < args.length; i++)
        {
            types[i] = args[i].getClass();
        }

        return createInstance(formType, types, args);
    }

    public <T extends SeleniumPage> T createPage(Class<T> pageType, Object... extraArgs)
    {
        if (extraArgs == null)
        {
            extraArgs = new Object[0];
        }

        Object[] args = new Object[extraArgs.length + 2];
        args[0] = selenium;
        args[1] = urls;
        System.arraycopy(extraArgs, 0, args, 2, extraArgs.length);

        Class[] types = new Class[extraArgs.length + 2];
        types[0] = Selenium.class;
        types[1] = Urls.class;

        for (int i = 2; i < args.length; i++)
        {
            types[i] = args[i].getClass();
        }

        return createInstance(pageType, types, args);
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

    public <T extends SeleniumPage> T open(Class<T> pageType, Object... extraArgs)
    {
        T page = createPage(pageType, extraArgs);
        page.open();
        return page;
    }

    public <T extends SeleniumPage> T waitFor(Class<T> pageType, Object... extraArgs)
    {
        T page = createPage(pageType, extraArgs);
        page.waitFor();
        return page;
    }

    public <T extends SeleniumPage> T openAndWaitFor(Class<T> pageType, Object... extraArgs)
    {
        T page = createPage(pageType, extraArgs);
        page.open();
        page.waitFor();
        return page;
    }

    //TODO: replace this with the use of pages
    public void goTo(String location)
    {
        selenium.open(StringUtils.join("/", true, baseUrl, location));
    }

    public void click(String locator)
    {
        selenium.click(locator);
    }

    public void waitForPageToLoad()
    {
        waitForPageToLoad(PAGELOAD_TIMEOUT);
    }

    public void waitForPageToLoad(long timeout)
    {
        selenium.waitForPageToLoad(Long.toString(timeout));
    }

    public boolean isElementPresent(String id)
    {
        return selenium.isElementPresent(StringUtils.toValidHtmlName(id));
    }

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

    public boolean isVisible(String locator)
    {
        return selenium.isVisible(locator);
    }

    public boolean bodyTextContains(String text)
    {
        return selenium.getBodyText().contains(text);
    }

    public String getBodyText()
    {
        return selenium.getBodyText();
    }

    public void refresh()
    {
        selenium.refresh();
    }

    public String getText(String locator)
    {
        return selenium.getText(locator);
    }

    public String[] getAllWindowTitles()
    {
        return selenium.getAllWindowTitles();
    }

    public void selectWindow(String title)
    {
        selenium.selectWindow(title);
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

        File failureFile = captureFailure(selenium);
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
            File failureFile = captureFailure(selenium);
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
            File failureFile = captureFailure(selenium);
            throw new RuntimeException(e.getMessage() + " (see: " + failureFile.getName() + ")", e);
        }
    }

    private File captureFailure(Selenium selenium)
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

    public Urls getUrls()
    {
        return urls;
    }
}
