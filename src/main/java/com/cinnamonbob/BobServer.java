package com.cinnamonbob;

import com.cinnamonbob.core.Bob;

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

        // initialise the shutdown service to allow this server
        // to be shutdown.
        shutdownService = new ShutdownService(adminPort, this);
        shutdownService.start();

        core = new Bob(System.getProperty("bob.home"));

        // initialise the build queue.
        buildQueue = new BuildQueue();
        buildQueue.setDispatcher(new BuildDispatcher()
        {
            public void dispatch(BuildRequest request)
            {
                core.build(request.getProjectName());
            }
        });

        // initialise jetty
        httpService = new HttpService(8080);
        httpService.start();

        // initialise Spring.
        
    }

    public void stop()
    {
        LOG.info("stop");
        shutdownService.stop();
        httpService.stop();
        buildQueue.stop();
    }

    public static void build(String projectName)
    {
        LOG.info("build");

        // request a build.
        buildQueue.enqueue(new BuildRequest(projectName));
    }
}
