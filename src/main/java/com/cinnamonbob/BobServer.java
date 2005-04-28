package com.cinnamonbob;

import com.cinnamonbob.core.Bob;
import com.cinnamonbob.bootstrap.StartupManager;
import com.cinnamonbob.bootstrap.BootstrapUtils;
import com.cinnamonbob.bootstrap.StartupException;
import com.cinnamonbob.bootstrap.jetty.JettyManager;

import java.util.logging.Logger;

/**
 * The BobServer handles the system initialisation / component
 * bootstrapping.
 *
 */
public class BobServer
{

    private static final Logger LOG = Logger.getLogger(BobServer.class.getName());

    private ShutdownService shutdownService = null;
    private HttpService httpService = null;

    private static BuildQueue buildQueue = null;
    private Bob core = null;

    private int adminPort;

    public BobServer(int port) {
        adminPort = port;
    }

    public void start() throws Exception
    {
        LOG.info("start");

        StartupManager.startupSystem();

        // initialise the shutdown service to allow this server
        // to be shutdown.
        shutdownService = new ShutdownService(adminPort, this);
        shutdownService.start();


        // initialise the build queue.
        buildQueue = new BuildQueue();
        buildQueue.setDispatcher(new BuildDispatcher()
        {
            public void dispatch(BuildRequest request)
            {
                core.build(request.getProjectName());
            }
        });

        core = new Bob(BootstrapUtils.getManager().getApplicationPaths().getApplicationRoot().getAbsolutePath());

        // initialise jetty


        JettyManager jettyManager = JettyManager.getInstance();
        jettyManager.deployWebapp();
        jettyManager.deployInWebApplicationContext("bob", core);
    }

    public void stop()
    {
        LOG.info("stop");
        shutdownService.stop();
//        httpService.stop();
        buildQueue.stop();
    }

    public static void build(String projectName)
    {
        LOG.info("build '" + projectName + "'");

        // request a build.
        buildQueue.enqueue(new BuildRequest(projectName));
    }
}
