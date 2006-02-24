package com.cinnamonbob;

import com.cinnamonbob.bootstrap.*;
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

    private ShutdownService shutdownService = null;

    public BobServer()
    {
    }

    public void start() throws Exception
    {
        LOG.info("start");

        SystemBootstrapManager bootstrap = new SystemBootstrapManager();
        bootstrap.bootstrapSystem();
    }

    public void stop()
    {
        LOG.info("stop");
        shutdownService.stop();
    }

    public static String getHostURL()
    {
        ApplicationConfiguration config = ConfigUtils.getManager().getAppConfig();
        if (config.getHostName() != null)
        {
            return config.getHostName();
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
