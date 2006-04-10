package com.zutubi.pulse.acceptance;

import junit.framework.TestCase;
import org.apache.xmlrpc.client.XmlRpcClient;
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;

import java.net.URL;

/**
 * <class-comment/>
 */
public class BaseXmlRpcAcceptanceTest extends TestCase
{
    protected XmlRpcClient xmlRpcClient;

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

        // test configuration.
        XmlRpcClientConfigImpl config = new XmlRpcClientConfigImpl();
            config.setServerURL(new URL("http", "localhost", 8080, "/xmlrpc"));

        xmlRpcClient = new XmlRpcClient();
        xmlRpcClient.setConfig(config);
    }

    protected void tearDown() throws Exception
    {
        xmlRpcClient = null;

        super.tearDown();
    }
}
