package com.cinnamonbob.api.clients;

import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;
import org.apache.xmlrpc.client.XmlRpcClient;
import org.apache.xmlrpc.XmlRpcException;

import java.net.URL;
import java.net.MalformedURLException;

import com.cinnamonbob.bootstrap.ConfigUtils;

/**
 * A simple client for sending a shutdown command to the Bob server.
 */
public class ShutdownClient
{
    public static void main(String argv[])
    {
        boolean force = false;

        if(argv.length > 1 && argv[1].equals("-f"))
        {
            force = true;
        }

        XmlRpcClientConfigImpl config = new XmlRpcClientConfigImpl();
        try
        {
            int webPort = 8080; //ConfigUtils.getManager().getAppConfig().getServerPort();
            config.setServerURL(new URL("http", "127.0.0.1", webPort, "/xmlrpc"));
        }
        catch (MalformedURLException e)
        {
            // Programmer error
            e.printStackTrace();
        }

        XmlRpcClient client = new XmlRpcClient();
        client.setConfig(config);
        Object[] params = new Object[]{ force };

        try
        {
            client.execute("BobRemoteApi.shutdown", params);
        }
        catch (XmlRpcException e)
        {
            System.err.println("Unable to send shutdown command to server: " + e.getMessage());
            System.exit(1);
        }

        System.exit(0);
    }
}
