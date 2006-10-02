package com.zutubi.pulse.command;

import com.zutubi.pulse.api.AdminTokenManager;
import com.zutubi.pulse.bootstrap.ComponentContext;
import com.zutubi.pulse.bootstrap.ConfigurationManager;
import com.zutubi.pulse.bootstrap.SystemBootstrapManager;
import com.zutubi.pulse.bootstrap.SystemConfiguration;
import com.zutubi.pulse.bootstrap.conf.EnvConfig;
import com.zutubi.pulse.bootstrap.conf.ConfigSupport;
import com.zutubi.pulse.bootstrap.conf.FileConfig;
import com.zutubi.pulse.util.IOUtils;
import com.opensymphony.util.TextUtils;
import org.apache.xmlrpc.XmlRpcClient;
import org.apache.xmlrpc.XmlRpcException;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

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

    private int port = -1;

    private String contextPath;

    private static final String ENV_PULSE_CONFIG = "PULSE_CONFIG";

    private String pulseConfig;

    private String loadAdminToken(ConfigurationManager configurationManager) throws IOException
    {
        File tokenFile = AdminTokenManager.getAdminTokenFilename(configurationManager.getSystemPaths().getConfigRoot());
        if (tokenFile.exists())
        {
            return IOUtils.fileToString(tokenFile);
        }
        return null;
    }

    public void setPort(int port)
    {
        this.port = port;
    }

    public void setContextPath(String contextPath)
    {
        this.contextPath = contextPath;
    }

    public void setConfig(String path)
    {
        this.pulseConfig = path;
    }

    public int execute()
    {
        // initialise the necessary resources
        // a) the xml rpc client
        // b) the admin token.

        if (TextUtils.stringSet(pulseConfig))
        {
            System.setProperty(EnvConfig.PULSE_CONFIG, pulseConfig);
        }
        else if (TextUtils.stringSet(System.getenv(ENV_PULSE_CONFIG)))
        {
            System.setProperty(EnvConfig.PULSE_CONFIG, System.getenv(ENV_PULSE_CONFIG));
        }

        SystemBootstrapManager sbm = new SystemBootstrapManager();
        sbm.loadBootstrapContext();

        ConfigurationManager configurationManager = (ConfigurationManager) ComponentContext.getBean("configurationManager");

        URL url;
        try
        {
            File configRoot = configurationManager.getSystemPaths().getConfigRoot();
            File startupConfigFile = new File(configRoot, "runtime.properties");
            ConfigSupport sysConfig = new ConfigSupport(new FileConfig(startupConfigFile));

            int webPort = sysConfig.getInteger(SystemConfiguration.WEBAPP_PORT);
            if (port != -1)
            {
                webPort = port;
            }

            String path = sysConfig.getProperty(SystemConfiguration.CONTEXT_PATH);
            if (TextUtils.stringSet(contextPath))
            {
                path = contextPath;
            }

            StringBuffer remoteApiPath = new StringBuffer();
            if (!path.startsWith("/"))
            {
                remoteApiPath.append("/");
            }
            remoteApiPath.append(path);
            if (!path.endsWith("/"))
            {
                remoteApiPath.append("/");
            }
            remoteApiPath.append("xmlrpc");

            url = new URL("http", "127.0.0.1", webPort, remoteApiPath.toString());
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