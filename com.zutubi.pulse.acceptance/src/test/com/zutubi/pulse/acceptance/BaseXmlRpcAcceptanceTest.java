package com.zutubi.pulse.acceptance;

import com.zutubi.util.RandomUtils;
import com.zutubi.pulse.core.test.api.PulseTestCase;

import java.net.URL;
import java.util.Vector;

/**
 * Helper base class for test cases that call the remote api.  Provides
 * simple functions for logging in and out and calling the API with a
 * token.
 */
public class BaseXmlRpcAcceptanceTest extends PulseTestCase
{
    public static final String SYMBOLIC_NAME_KEY = XmlRpcHelper.SYMBOLIC_NAME_KEY;

    XmlRpcHelper xmlRpcHelper;

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

        xmlRpcHelper = new XmlRpcHelper(new URL("http", "localhost", port, "/xmlrpc"));
    }

    protected String randomName()
    {
        return getName() + "-" + RandomUtils.randomString(10);
    }

    public String login(String login, String password) throws Exception
    {
        return xmlRpcHelper.login(login, password);
    }

    public String loginAsAdmin() throws Exception
    {
        return xmlRpcHelper.loginAsAdmin();
    }

    public boolean logout() throws Exception
    {
        return xmlRpcHelper.logout();
    }

    public Vector<Object> getVector(Object... o)
    {
        return xmlRpcHelper.getVector(o);
    }

    public <T> T callWithoutToken(String function, Object... args) throws Exception
    {
        return xmlRpcHelper.<T>callWithoutToken(function, args);
    }

    public <T> T call(String function, Object... args) throws Exception
    {
        return xmlRpcHelper.<T>call(function, args);
    }

    public String insertSimpleProject(String name) throws Exception
    {
        return xmlRpcHelper.insertSimpleProject(name, false);
    }

    public boolean ensureProject(String name) throws Exception
    {
        return xmlRpcHelper.ensureProject(name);
    }

    public String insertSimpleAgent(String name) throws Exception
    {
        return xmlRpcHelper.insertSimpleAgent(name);
    }

    protected void callAndExpectError(String error, String function, Object... args)
    {
        try
        {
            xmlRpcHelper.call(function, args);
            fail();
        }
        catch (Exception e)
        {
            assertTrue("Message '" + e.getMessage() + "' does not contain '" + error + "'", e.getMessage().contains(error));
        }
    }
}
