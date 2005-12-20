package com.cinnamonbob.slave;

import com.cinnamonbob.bootstrap.ComponentContext;
import com.cinnamonbob.bootstrap.SystemBootstrapManager;
import com.cinnamonbob.jetty.JettyManager;

import com.cinnamonbob.util.logging.Logger;

/**
 */
public class SlaveServer
{
    private static final Logger LOG = Logger.getLogger(SlaveServer.class);

    public SlaveServer()
    {

    }

    public void start()
    {
        LOG.info("start");
        SystemBootstrapManager bootstrap = new SystemBootstrapManager();
        bootstrap.bootstrapSystem();
    }

    public void stop()
    {
        LOG.info("stop");
        ((JettyManager) ComponentContext.getBean("jettyManager")).stop();
    }

    public static void main(String argv[])
    {
        SlaveServer server = new SlaveServer();
        server.start();
    }
}
