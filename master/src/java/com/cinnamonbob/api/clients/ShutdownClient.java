package com.cinnamonbob.api.clients;

import org.apache.xmlrpc.XmlRpcException;
import org.apache.xmlrpc.client.XmlRpcClient;
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * A simple client for sending a shutdown command to the Bob server.
 */
public class ShutdownClient
{
    public static void main(String argv[])
    {
        boolean force = false;

        if (argv.length < 2)
        {
            System.err.println("Admin username and password must be specified");
            System.exit(1);
        }

        if (argv.length > 2 && argv[2].equals("force"))
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

        try
        {
            String token = (String) client.execute("RemoteApi.login", new Object[]{argv[0], argv[1]});
            client.execute("RemoteApi.shutdown", new Object[]{token, force});
        }
        catch (XmlRpcException e)
        {
            System.err.println("Unable to send shutdown command to server: " + e.getMessage());
            System.exit(1);
        }

        System.exit(0);
    }
}
