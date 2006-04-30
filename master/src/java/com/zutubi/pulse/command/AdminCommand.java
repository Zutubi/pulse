/********************************************************************************
 @COPYRIGHT@
 ********************************************************************************/
package com.zutubi.pulse.command;

import org.apache.xmlrpc.XmlRpcClient;
import org.apache.xmlrpc.XmlRpcException;
import com.zutubi.pulse.bootstrap.ConfigurationManager;
import com.zutubi.pulse.bootstrap.SystemBootstrapManager;
import com.zutubi.pulse.bootstrap.ComponentContext;
import com.zutubi.pulse.api.TokenManager;
import com.zutubi.pulse.util.IOUtils;

import java.io.IOException;
import java.io.File;
import java.net.URL;
import java.net.MalformedURLException;

/**
 * The abstract base command for commands that are run on the same host as the
 * running server and that require the admin token to authenticate themselves.
 *
 * @author Daniel Ostermeier
 */
public abstract class AdminCommand implements Command
{
    /**
     * The xml rpc client used to connect to the server.
     */
    protected XmlRpcClient xmlRpcClient;

    /**
     * The admin token that allows the xml rpc request to be authenticated as a local request.
     */
    protected String adminToken;

    private String loadAdminToken(ConfigurationManager configurationManager) throws IOException
    {
        File tokenFile = TokenManager.getAdminTokenFilename(configurationManager);
        return IOUtils.fileToString(tokenFile);
    }

    public int execute()
    {
        // initialise the necessary resources
        // a) the xml rpc client
        // b) the admin token.

        SystemBootstrapManager.loadBootstrapContext();
        ConfigurationManager configurationManager = (ConfigurationManager) ComponentContext.getBean("configurationManager");

        URL url;
        try
        {
            int webPort = configurationManager.getAppConfig().getServerPort();
            url = new URL("http", "127.0.0.1", webPort, "/xmlrpc");
        }
        catch (MalformedURLException e)
        {
            // Programmer error
            e.printStackTrace();
            return 1;
        }

        xmlRpcClient = new XmlRpcClient(url);

        try
        {
            adminToken = loadAdminToken(configurationManager);
            return doExecute();
        }
        catch (IOException e)
        {
            System.err.println("I/O Error: " + e.getMessage());
            return 1;
        }
        catch (XmlRpcException e)
        {
            System.err.println("Unable to send shutdown command to server: " + e.getMessage());
            return 1;
        }
    }

    /**
     * Admin command implementations should implement there custom functionality
     * in this method. When this method is invoked, both the XmlRpcClient and the
     * AdminToken will be available.
     */
    public abstract int doExecute() throws XmlRpcException, IOException;

}