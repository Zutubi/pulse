package com.cinnamonbob;

import com.cinnamonbob.bootstrap.SystemBootstrapManager;
import com.cinnamonbob.util.logging.Logger;

/**
 * The BobServer handles the system initialisation / component
 * bootstrapping.
 *
 * @deprecated
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
}
