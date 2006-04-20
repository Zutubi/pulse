/********************************************************************************
 @COPYRIGHT@
 ********************************************************************************/
package com.zutubi.pulse.acceptance;

import junit.framework.TestCase;
import org.apache.xmlrpc.client.XmlRpcClient;
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;
import org.apache.xmlrpc.XmlRpcClient;

import java.net.URL;
import java.util.Vector;
import java.util.Arrays;

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

    protected Vector<Object> getVector(Object... o)
    {
        return new Vector<Object>(Arrays.asList(o));
    }
}
