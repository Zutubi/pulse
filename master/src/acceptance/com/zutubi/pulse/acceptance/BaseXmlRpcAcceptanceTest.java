package com.zutubi.pulse.acceptance;

import junit.framework.TestCase;
import org.apache.xmlrpc.XmlRpcClient;

import java.net.URL;
import java.util.Vector;
import java.util.Arrays;
import java.util.Hashtable;

import com.zutubi.util.RandomUtils;

/**
 * Helper base class for test cases that call the remote api.  Provides
 * simple functions for logging in and out and calling the API with a
 * token.
 */
public class BaseXmlRpcAcceptanceTest extends TestCase
{
    public static final String SYMBOLIC_NAME_KEY = "meta.symbolicName";
    
    protected XmlRpcClient xmlRpcClient;
    protected String token = null;

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

        // test configuration.
        xmlRpcClient = new XmlRpcClient(new URL("http", "localhost", port, "/xmlrpc"));
    }

    protected void tearDown() throws Exception
    {
        xmlRpcClient = null;
        super.tearDown();
    }

    protected String randomName()
    {
        return RandomUtils.randomString(10);
    }

    protected String login(String login, String password) throws Exception
    {
        token = (String) callWithoutToken("login", login, password);
        return token;
    }

    protected String loginAsAdmin() throws Exception
    {
        return login("admin", "admin");
    }

    protected boolean logout() throws Exception
    {
        verifyLoggedIn();
        Object result = callWithoutToken("logout", token);
        token = null;
        return (Boolean)result;
    }

    protected Vector<Object> getVector(Object... o)
    {
        return new Vector<Object>(Arrays.asList(o));
    }

    @SuppressWarnings({ "unchecked" })
    protected <T> T callWithoutToken(String function, Object... args) throws Exception
    {
        return (T) xmlRpcClient.execute("RemoteApi." + function, getVector(args));
    }

    @SuppressWarnings({ "unchecked" })
    protected <T> T call(String function, Object... args) throws Exception
    {
        verifyLoggedIn();
        Vector<Object> argVector = new Vector<Object>(args.length + 1);
        argVector.add(token);
        argVector.addAll(Arrays.asList(args));
        return (T) xmlRpcClient.execute("RemoteApi." + function, argVector);
    }

    private void verifyLoggedIn()
    {
        if(token == null)
        {
            throw new IllegalStateException("Not logged in, call login first");
        }
    }

    protected String insertSimpleProject(String name) throws Exception
    {
        Hashtable<String, Object> scm = new Hashtable<String, Object>();
        scm.put(SYMBOLIC_NAME_KEY, "zutubi.svnConfig");
        scm.put("url", "svn://localhost/test/trunk");
        scm.put("monitor", false);

        Hashtable<String, Object> type = new Hashtable<String, Object>();
        type.put(SYMBOLIC_NAME_KEY, "zutubi.antTypeConfig");
        type.put("file", "build.xml");

        Hashtable<String, Object> project = new Hashtable<String, Object>();
        project.put(SYMBOLIC_NAME_KEY, "zutubi.projectConfig");
        project.put("name", name);
        project.put("scm", scm);
        project.put("type", type);

        return call("insertTemplatedConfig", "projects/global project template", project, false);
    }
}
