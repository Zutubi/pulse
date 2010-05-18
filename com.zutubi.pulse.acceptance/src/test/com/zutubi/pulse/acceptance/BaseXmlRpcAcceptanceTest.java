package com.zutubi.pulse.acceptance;

import com.zutubi.pulse.core.test.api.PulseTestCase;
import com.zutubi.util.RandomUtils;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.GetMethod;

import java.io.IOException;
import java.util.Vector;

/**
 * Helper base class for test cases that call the remote api.  Provides
 * simple functions for logging in and out and calling the API with a
 * token.
 */
public class BaseXmlRpcAcceptanceTest extends PulseTestCase
{
    public static final String SYMBOLIC_NAME_KEY = XmlRpcHelper.SYMBOLIC_NAME_KEY;

    protected XmlRpcHelper xmlRpcHelper;
    protected String baseUrl;

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

        int port = AcceptanceTestUtils.getPulsePort();
        baseUrl = "http://localhost:" + port + "/";
        xmlRpcHelper = new XmlRpcHelper();
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

    protected String downloadAsAdmin(String url) throws IOException
    {
        HttpClient client = new HttpClient();
        client.getState().setCredentials(new AuthScope(null, -1), AcceptanceTestUtils.ADMIN_CREDENTIALS);
        client.getParams().setAuthenticationPreemptive(true);

        GetMethod get = new GetMethod(url);
        get.setDoAuthentication(true);
        try
        {
            client.executeMethod(get);
            return get.getResponseBodyAsString();
        }
        finally
        {
            get.releaseConnection();
        }
    }
}
