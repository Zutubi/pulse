package com.cinnamonbob;

import com.cinnamonbob.core.Bob;
import com.cinnamonbob.core.ConfigException;

import java.util.logging.Logger;

/**
 * @author Daniel Ostermeier
 */
public class BobServer
{

    private static final Logger LOG = Logger.getLogger(BobServer.class.getName());

    private AdminService adminService = null;

    private int adminPort;

    public BobServer(int port) {
        adminPort = port;
    }

    public void start() throws Exception
    {
        LOG.info("start");
        adminService = new AdminService(adminPort, this);
        adminService.start();
    }

    public void stop()
    {
        LOG.info("stop");
        adminService.stop();
    }

    public void build()
    {
        LOG.info("build");
        // execute a build...
        try {
            new Bob(System.getProperty("root.dir"));
        } catch (ConfigException e) {
            e.printStackTrace();
        }
    }
}
