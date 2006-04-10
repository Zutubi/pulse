package com.zutubi.pulse.slave;

import com.zutubi.pulse.bootstrap.ComponentContext;
import com.zutubi.pulse.bootstrap.SystemBootstrapManager;
import com.zutubi.pulse.jetty.JettyManager;
import com.zutubi.pulse.util.logging.Logger;

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
        ((JettyManager) ComponentContext.getBean("jettyManager")).stop(true);
    }

    public static void main(String argv[])
    {
        SlaveServer server = new SlaveServer();
        server.start();
    }
}
