package com.zutubi.pulse.acceptance;

import com.thoughtworks.selenium.DefaultSelenium;
import com.thoughtworks.selenium.Selenium;
import com.zutubi.util.CollectionUtils;
import junit.framework.TestCase;

/**
 */
public class SeleniumTestBase extends TestCase
{
    protected Selenium selenium;
    protected String port;
    
    protected void setUp() throws Exception
    {
        super.setUp();

        port = System.getProperty("pulse.port");
        if(port == null)
        {
            port = "8080";
        }

        selenium = new DefaultSelenium("localhost", 4444, "*firefox", "http://localhost:" + port + "/");
        selenium.start();
    }

    protected void tearDown() throws Exception
    {
        selenium.stop();
        selenium = null;
        super.tearDown();
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
}
