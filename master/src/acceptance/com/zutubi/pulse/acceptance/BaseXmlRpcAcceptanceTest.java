package com.zutubi.pulse.acceptance;

import com.zutubi.util.RandomUtils;
import junit.framework.TestCase;

import java.net.URL;
import java.util.Vector;

/**
 * Helper base class for test cases that call the remote api.  Provides
 * simple functions for logging in and out and calling the API with a
 * token.
 */
public class BaseXmlRpcAcceptanceTest extends TestCase
{
    public static final String SYMBOLIC_NAME_KEY = XmlRpcHelper.SYMBOLIC_NAME_KEY;

    XmlRpcHelper helper;

    public BaseXmlRpcAcceptanceTest()
    {
    }

    public BaseXmlRpcAcceptanceTest(String testName)
    {
        super(testName);
    }

    protected void setUp() throws Exception
    {
        super.setUp();

        int port = 8080;

        String portProperty = System.getProperty("pulse.port");
        if(portProperty != null)
        {
            port = Integer.parseInt(portProperty);
        }

        helper = new XmlRpcHelper(new URL("http", "localhost", port, "/xmlrpc"));
    }

    protected void tearDown() throws Exception
    {
        helper = null;
        super.tearDown();
    }

    protected String randomName()
    {
        return RandomUtils.randomString(10);
    }

    public String login(String login, String password) throws Exception
    {
        return helper.login(login, password);
    }

    public String loginAsAdmin() throws Exception
    {
        return helper.loginAsAdmin();
    }

    public boolean logout() throws Exception
    {
        return helper.logout();
    }

    public Vector<Object> getVector(Object... o)
    {
        return helper.getVector(o);
    }

    public <T> T callWithoutToken(String function, Object... args) throws Exception
    {
        return helper.<T>callWithoutToken(function, args);
    }

    public <T> T call(String function, Object... args) throws Exception
    {
        return helper.<T>call(function, args);
    }

    public String insertSimpleProject(String name) throws Exception
    {
        return helper.insertSimpleProject(name, false);
    }

    public String ensureProject(String name) throws Exception
    {
        return helper.ensureProject(name);
    }

    public String insertSimpleAgent(String name) throws Exception
    {
        return helper.insertSimpleAgent(name);
    }

    protected void callAndExpectError(String error, String function, Object... args)
    {
        try
        {
            helper.call(function, args);
            fail();
        }
        catch (Exception e)
        {
            assertTrue("Message '" + e.getMessage() + "' does not contain '" + error + "'", e.getMessage().contains(error));
        }
    }
}
