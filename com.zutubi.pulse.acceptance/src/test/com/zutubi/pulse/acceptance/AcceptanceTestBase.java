package com.zutubi.pulse.acceptance;

import com.zutubi.pulse.core.test.api.PulseTestCase;
import com.zutubi.pulse.master.webwork.Urls;
import com.zutubi.util.RandomUtils;

import java.io.File;

import static com.zutubi.pulse.acceptance.AcceptanceTestUtils.getPulseUrl;

/**
 *
 */
public abstract class AcceptanceTestBase extends PulseTestCase
{
    /**
     * Shared agent used for simple single-agent builds.  Makes it easier to
     * run these tests in development environments (just manually run one
     * agent on port 8890).
     */
    public static final String AGENT_NAME = "localhost";

    protected XmlRpcHelper xmlRpcHelper;
    protected String baseUrl;

    protected Urls urls;
    protected String random;

    private SeleniumBrowser browser;
    private SeleniumBrowserFactory browserFactory;

    protected void setUp() throws Exception
    {
        super.setUp();

        baseUrl = getPulseUrl();
        xmlRpcHelper = new XmlRpcHelper();
        random = randomName();

        browserFactory = new DefaultSeleniumBrowserFactory();
        browserFactory.cleanup();

        urls = new Urls(baseUrl);
    }

    protected void tearDown() throws Exception
    {
        browserFactory.cleanup();
        if (xmlRpcHelper.isLoggedIn())
        {
            xmlRpcHelper.logout();
        }
        super.tearDown();
    }

    protected String randomName()
    {
        return getName() + "-" + RandomUtils.randomString(10);
    }

    public SeleniumBrowser getBrowser()
    {
        if (browser == null)
        {
            browser = browserFactory.newBrowser();
        }
        return browser;
    }

    /**
     * Runs the bare test sequence, capturing a screenshot if a test fails
     *
     * @throws Throwable if any exception is thrown
     */
    // @Override
    public void runBare() throws Throwable
    {
        setUp();
        try
        {
            runTest();
        }
        catch (Throwable t)
        {
            if (browser != null)
            {
                String filename = getName() + ".png";
                try
                {
                    browser.captureScreenshot(new File("working", filename));
                    System.err.println("Saved screenshot " + filename);
                }
                catch (Exception e)
                {
                    System.err.println("Couldn't save screenshot " + filename + ": " + e.getMessage());
                    e.printStackTrace();
                }
                throw t;
            }
        }
        finally
        {
            tearDown();
        }
    }


}
