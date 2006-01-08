package com.cinnamonbob;

import com.cinnamonbob.bootstrap.ComponentContext;
import com.cinnamonbob.bootstrap.ConfigUtils;
import com.cinnamonbob.bootstrap.ConfigurationManager;
import com.cinnamonbob.bootstrap.SystemBootstrapManager;
import com.cinnamonbob.model.ProjectManager;
import com.cinnamonbob.util.logging.Logger;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * The BobServer handles the system initialisation / component
 * bootstrapping.
 */
public class BobServer
{
    private static final Logger LOG = Logger.getLogger(BobServer.class);
    private static final String HOST_NAME_PROPERTY = "host.name";

    private ShutdownService shutdownService = null;

    public BobServer()
    {
    }

    public void start() throws Exception
    {
        LOG.info("start");

        SystemBootstrapManager bootstrap = new SystemBootstrapManager();
        bootstrap.bootstrapSystem();

        int adminPort = ConfigUtils.getManager().getAppConfig().getAdminPort();
        // initialise the shutdown service to allow this server
        // to be shutdown.
        shutdownService = new ShutdownService(adminPort, this);
        shutdownService.start();

        ProjectManager projectManager = (ProjectManager) ComponentContext.getBean("projectManager");
        projectManager.initialise();

    }

    public void stop()
    {
        LOG.info("stop");
        shutdownService.stop();
    }

    public static String getHostURL()
    {
        ConfigurationManager config = ConfigUtils.getManager();
        if (config.hasProperty(HOST_NAME_PROPERTY))
        {
            return config.lookupProperty(HOST_NAME_PROPERTY);
        }
        else
        {
            InetAddress address;
            String result = null;

            try
            {
                address = InetAddress.getLocalHost();
                result = address.getCanonicalHostName();
            }
            catch (UnknownHostException e)
            {
                LOG.warning("Could not obtain local host name", e);
            }

            return result;
        }
    }
}
