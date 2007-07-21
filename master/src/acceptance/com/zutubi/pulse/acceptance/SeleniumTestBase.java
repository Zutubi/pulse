package com.zutubi.pulse.acceptance;

import com.thoughtworks.selenium.DefaultSelenium;
import com.thoughtworks.selenium.Selenium;
import com.zutubi.util.CollectionUtils;
import com.zutubi.util.RandomUtils;
import junit.framework.TestCase;

/**
 * Helper base class for web UI acceptance tests that use Selenium.
 */
public class SeleniumTestBase extends TestCase
{
    protected Selenium selenium;
    protected String port;
    protected String baseUrl;
    protected String random;

    protected void setUp() throws Exception
    {
        super.setUp();

        port = System.getProperty("pulse.port", "8080");
        baseUrl = "http://localhost:" + port + "/";
        random = RandomUtils.randomString(10);
        selenium = new DefaultSelenium("localhost", 4444, "*firefox", "http://localhost:" + port + "/");
        selenium.start();
    }

    protected void tearDown() throws Exception
    {
        selenium.stop();
        selenium = null;
        super.tearDown();
    }

    private void login(String username, String password)
    {
        goTo(Navigation.LOCATION_LOGIN);
        selenium.type("j_username", username);
        selenium.type("j_password", password);
        selenium.click("login");
    }

    protected void loginAsAdmin()
    {
        login("admin", "admin");
    }

    protected void goTo(String location)
    {
        selenium.open(baseUrl + location);
    }

    protected void assertTextPresent(String text)
    {
        assertTrue(selenium.isTextPresent(text));
    }
    
    protected void assertLinkPresent(String id)
    {
        assertTrue(CollectionUtils.contains(selenium.getAllLinks(), id));
    }

    protected void assertFormFieldNotEmpty(String id)
    {
        String value = selenium.getValue(id);
        assertNotNull(value);
        assertTrue(value.length() > 0);
    }

    protected void waitForElement(String id)
    {
        SeleniumUtils.waitForElement(selenium, id);
    }

    protected void waitForElement(String id, long timeout)
    {
        SeleniumUtils.waitForElement(selenium, id, timeout);
    }
}
