package com.cinnamonbob;

import com.cinnamonbob.bootstrap.ConfigUtils;
import com.cinnamonbob.bootstrap.SystemBootstrapManager;
import com.cinnamonbob.core2.BuildProcessor;

import java.net.InetAddress;
import java.net.UnknownHostException;
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

    private static BuildQueue buildQueue = null;
    private BuildProcessor buildProcessor = null;

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

        buildProcessor = new BuildProcessor();

        // initialise the build queue.
        buildQueue = new BuildQueue();
        buildQueue.setDispatcher(new BuildDispatcher()
        {
            public void dispatch(BuildRequest request)
            {
                buildProcessor.execute(request);
            }
        });

//        core = new Bob();
//
//        // initialise jetty
//
//
//        JettyManager jettyManager = JettyManager.getInstance();
//        jettyManager.deployWebapp();
//        jettyManager.deployInWebApplicationContext("bob", core);
//        jettyManager.deployInWebApplicationContext("server", this);
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
    
    public BuildQueue getBuildQueue()
    {
        return buildQueue;
    }
    
    public String getHostURL()
    {
        InetAddress address;
        try
        {
            address = InetAddress.getLocalHost();
            return address.getHostName();
        }
        catch(UnknownHostException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        return null;
    }
}
