package com.cinnamonbob.api;

import junit.framework.TestCase;
import org.apache.xmlrpc.WebServer;
import org.apache.xmlrpc.XmlRpcClient;
import org.apache.xmlrpc.XmlRpcServer;

/**
 * XmlRpcServer
 */
public class XmlRpcApiHandlerTest extends TestCase
{

    private XmlRpcServer server = null;
    private XmlRpcApiHandler handler = null;
    private XmlRpcClient client = null;
    private WebServer webServer = null;

    public XmlRpcApiHandlerTest(String testName)
    {
        super(testName);
    }

    public void setUp() throws Exception
    {
//        handler = new XmlRpcApiHandler();
//        server = new XmlRpcServer();
//        server.addHandler("$default", handler);
//
//        InetAddress localhost = InetAddress.getLocalHost();
//        webServer = new WebServer(5445, localhost);
//        webServer.addHandler("$default", handler);
//        webServer.start();
//
//        client = new XmlRpcClient(localhost.getHostName(), 5445);
    }

    public void tearDown() throws Exception
    {
//        webServer.shutdown();
    }

    public void testBuildRpc()
    {
//        String rpc = "<?xml version=\"1.0\"?>\n" +
//        "<methodCall>\n" +
//        "   <methodName>build</methodName>\n" +
//        "   <params>" +
//        "       <param><value>testProject</value></param>" +
//        "   </params>\n" +
//        "</methodCall>\n";
//
//        String response = new String(server.initialise(new ByteArrayInputStream(rpc.getBytes())));
//
//        // check that a build request has been generated for the
//        // appropriate project.
//
    }

    public void testBuildRpcViaHttpRequest() throws Exception
    {
//        Vector<String> params = new Vector<String>();
//        params.add("testProject");
//        client.initialise("build", params);
    }
}
