package com.cinnamonbob;

import com.cinnamonbob.bootstrap.ComponentContext;
import com.cinnamonbob.bootstrap.ConfigUtils;
import com.cinnamonbob.bootstrap.ConfigurationManager;
import com.cinnamonbob.bootstrap.SystemBootstrapManager;
import com.cinnamonbob.core.BuildProcessor;
import com.cinnamonbob.model.ProjectManager;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The BobServer handles the system initialisation / component
 * bootstrapping.
 *
 */
public class BobServer
{
    private static final Logger LOG = Logger.getLogger(BobServer.class.getName());
    private static final String HOST_NAME_PROPERTY = "host.name";

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

        buildProcessor = (BuildProcessor)ComponentContext.getBean("buildProcessor");

        // initialise the build queue.
        buildQueue = new BuildQueue();
        buildQueue.setDispatcher(new BuildDispatcher()
        {
            public void dispatch(BuildRequest request)
            {
                buildProcessor.execute(request);
            }
        });
        
        ProjectManager projectManager = (ProjectManager)ComponentContext.getBean("projectManager");
        projectManager.initialise();
    }

    public void stop()
    {
        LOG.info("stop");
        shutdownService.stop();
        buildQueue.stop();
    }
    
    public static void build(String projectName)
    {
        LOG.info("build '" + projectName + "'");
        buildQueue.enqueue(new BuildRequest(projectName));
    }
    
    public static void build(String projectName, String recipeName)
    {
        LOG.info("build '" + projectName + ":" + recipeName + "'");
        buildQueue.enqueue(new BuildRequest(projectName, recipeName));
    }
    
    public BuildQueue getBuildQueue()
    {
        return buildQueue;
    }
    
    public static String getHostURL()
    {
        ConfigurationManager config = ConfigUtils.getManager();
        if(config.hasProperty(HOST_NAME_PROPERTY))
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
            catch(UnknownHostException e)
            {
                LOG.log(Level.WARNING, "Could not obtain local host name", e);
            }
        
            return result;
        }
    }
}
