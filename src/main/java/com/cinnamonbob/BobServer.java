package com.cinnamonbob;

import com.cinnamonbob.core.Bob;

import java.util.logging.Logger;

/**
 * @author Daniel Ostermeier
 */
public class BobServer
{

    private static final Logger LOG = Logger.getLogger(BobServer.class.getName());

    private AdminService adminService = null;
    private HttpService httpService = null;

    private BuildQueue buildQueue = null;
    private Bob core = null;

    private int adminPort;

    public BobServer(int port) {
        adminPort = port;
    }

    public void start() throws Exception
    {
        LOG.info("start");
        
        adminService = new AdminService(adminPort, this);
        adminService.start();

        core = new Bob(System.getProperty("bob.home"));

        buildQueue = new BuildQueue();
        buildQueue.setDispatcher(new BuildDispatcher()
        {
            public void dispatch(BuildRequest request)
            {
                core.build(request.getProjectName());
            }
        });

        httpService = new HttpService(8080);
        httpService.start();
    }

    public void stop()
    {
        LOG.info("stop");
        adminService.stop();
        httpService.stop();
    }

    public void build(String projectName)
    {
        LOG.info("build");

        // request a build.
        buildQueue.enqueue(new BuildRequest(projectName));
    }
}
