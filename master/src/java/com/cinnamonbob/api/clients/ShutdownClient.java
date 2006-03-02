package com.cinnamonbob.api.clients;

import com.cinnamonbob.api.TokenManager;
import com.cinnamonbob.bootstrap.ComponentContext;
import com.cinnamonbob.bootstrap.ConfigurationManager;
import com.cinnamonbob.bootstrap.SystemBootstrapManager;
import com.cinnamonbob.core.util.IOUtils;
import org.apache.xmlrpc.XmlRpcException;
import org.apache.xmlrpc.client.XmlRpcClient;
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;

import java.io.File;
import java.io.IOException;
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

        if (argv.length > 0 && argv[0].equals("force"))
        {
            force = true;
        }

        SystemBootstrapManager.loadBootstrapContext();
        ConfigurationManager configurationManager = (ConfigurationManager) ComponentContext.getBean("configurationManager");

        XmlRpcClientConfigImpl config = new XmlRpcClientConfigImpl();
        try
        {
            int webPort = configurationManager.getAppConfig().getServerPort();
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
            String token = loadAdminToken(configurationManager);
            client.execute("RemoteApi.shutdown", new Object[]{token, force});
        }
        catch (IOException e)
        {
            System.err.println("Error opening admin token file: " + e.getMessage());
            System.exit(1);
        }
        catch (XmlRpcException e)
        {
            System.err.println("Unable to send shutdown command to server: " + e.getMessage());
            System.exit(1);
        }

        System.exit(0);
    }

    private static String loadAdminToken(ConfigurationManager configurationManager) throws IOException
    {
        File tokenFile = TokenManager.getAdminTokenFilename(configurationManager);
        return IOUtils.fileToString(tokenFile);
    }
}
