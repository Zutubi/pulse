package com.zutubi.pulse.servercore.cli;

import com.zutubi.pulse.command.BootContext;
import com.zutubi.pulse.command.Command;
import com.zutubi.pulse.core.util.config.EnvConfig;
import com.zutubi.pulse.servercore.api.AdminTokenManager;
import com.zutubi.pulse.servercore.bootstrap.ConfigurationManager;
import com.zutubi.pulse.servercore.bootstrap.SystemBootstrapManager;
import com.zutubi.pulse.servercore.bootstrap.SystemConfiguration;
import com.zutubi.util.StringUtils;
import com.zutubi.util.config.ConfigSupport;
import com.zutubi.util.config.FileConfig;
import com.zutubi.util.io.IOUtils;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.xmlrpc.XmlRpcClient;
import org.apache.xmlrpc.XmlRpcException;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * The abstract base command for commands that are run on the same host as the
 * running server and that require the admin token to authenticate themselves.
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

    public int execute(BootContext context) throws ParseException, IOException
    {
        return execute(context.getCommandArgv());
    }

    public int execute(String... argv) throws ParseException, IOException
    {
        // initialise the necessary resources
        // a) the xml rpc client
        // b) the admin token.

        if (StringUtils.stringSet(pulseConfig))
        {
            System.setProperty(EnvConfig.PULSE_CONFIG, pulseConfig);
        }
        else if (StringUtils.stringSet(System.getenv(ENV_PULSE_CONFIG)))
        {
            System.setProperty(EnvConfig.PULSE_CONFIG, System.getenv(ENV_PULSE_CONFIG));
        }

        SystemBootstrapManager sbm = new SystemBootstrapManager();
        sbm.loadBootstrapContext();
        ConfigurationManager configurationManager = sbm.getConfigurationManager();

        URL url;
        try
        {
            File configRoot = configurationManager.getSystemPaths().getConfigRoot();
            SystemConfiguration config = configurationManager.getSystemConfig();
            File startupConfigFile = new File(configRoot, "runtime.properties");
            ConfigSupport sysConfig = new ConfigSupport(new FileConfig(startupConfigFile));

            int webPort = sysConfig.getInteger(SystemConfiguration.WEBAPP_PORT, config.getServerPort());
            if (port != -1)
            {
                webPort = port;
            }

            String path = sysConfig.getProperty(SystemConfiguration.CONTEXT_PATH, config.getContextPath());
            if (StringUtils.stringSet(contextPath))
            {
                path = contextPath;
            }

            StringBuffer remoteApiPath = new StringBuffer();
            if (!path.startsWith("/"))
            {
                remoteApiPath.append("/");
            }
            remoteApiPath.append(path);
            if (!remoteApiPath.toString().endsWith("/"))
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
            return doExecute(argv);
        }
        catch (IOException e)
        {
            System.err.println("I/O Error: " + e.getMessage());
            return 1;
        }
        catch (XmlRpcException e)
        {
            System.err.println("Unable to send command to server: " + e.getMessage());
            return 1;
        }
    }


    public List<String> getUsages()
    {
        return Arrays.asList("");
    }

    public Map<String, String> getOptions()
    {
        Map<String, String> options = new LinkedHashMap<String, String>();
        options.put("-p [--port] port", "the port to be used by the pulse web interface");
        options.put("-c [--contextPath] path", "the pulse web application context path");
        options.put("-f [--config] file", "specify an alternate config file");
        return options;
    }

    public boolean isDefault()
    {
        return false;
    }

    /**
     * Admin command implementations should implement their custom functionality
     * in this method. When this method is invoked, both the XmlRpcClient and the
     * AdminToken will be available.
     */
    public abstract int doExecute(String[] argv) throws XmlRpcException, IOException, ParseException;

    @SuppressWarnings({ "AccessStaticViaInstance" })
    protected Options getSharedOptions()
    {
        Options options = new Options();
        options.addOption(OptionBuilder.withLongOpt("port")
                .hasArg()
                .create('p'));
        options.addOption(OptionBuilder.withLongOpt("contextpath")
                .hasArg()
                .create('c'));
        options.addOption(OptionBuilder.withLongOpt("config")
                .hasArg()
                .create('f'));
        return options;
    }

    protected void processSharedOptions(CommandLine commandLine)
    {
        if (commandLine.hasOption('p'))
        {
            setPort(Integer.parseInt(commandLine.getOptionValue('p')));
        }
        if (commandLine.hasOption('c'))
        {
            setContextPath(commandLine.getOptionValue('c'));
        }
        if (commandLine.hasOption('f'))
        {
            setConfig(commandLine.getOptionValue('f'));
        }
    }
}