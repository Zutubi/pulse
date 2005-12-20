package com.cinnamonbob;

import com.cinnamonbob.bootstrap.ComponentContext;
import com.cinnamonbob.bootstrap.ConfigUtils;
import com.cinnamonbob.bootstrap.ConfigurationManager;
import com.cinnamonbob.bootstrap.SystemBootstrapManager;
import com.cinnamonbob.model.ProjectManager;
import com.cinnamonbob.model.Slave;
import com.cinnamonbob.model.persistence.SlaveDao;
import com.cinnamonbob.services.SlaveService;
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
    private static final String HOST_NAME_PROPERTY = "host.name";

    private ShutdownService shutdownService = null;

    private static BuildQueue buildQueue = null;
    private MasterBuildProcessor masterBuildProcessor = null;

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

        masterBuildProcessor = (MasterBuildProcessor) ComponentContext.getBean("masterBuildProcessor");

        // initialise the build queue.
        buildQueue = (BuildQueue) ComponentContext.getBean("buildQueue");
        buildQueue.setDispatcher(new BuildDispatcher()
        {
            public void dispatch(BuildRequest request)
            {
                masterBuildProcessor.execute(request);
            }
        });

        ProjectManager projectManager = (ProjectManager) ComponentContext.getBean("projectManager");
        projectManager.initialise();

        //DodgyPinger pinger = new DodgyPinger((SlaveDao) ComponentContext.getBean("slaveDao"), (SlaveProxyFactory) ComponentContext.getBean("slaveProxyFactory"));
        //Thread t = new Thread(pinger);
        //t.start();
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
        if (config.hasProperty(HOST_NAME_PROPERTY))
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
            catch (UnknownHostException e)
            {
                LOG.warning("Could not obtain local host name", e);
            }

            return result;
        }
    }

    public class DodgyPinger implements Runnable
    {
        SlaveDao slaveDao;
        private SlaveProxyFactory factory;

        public DodgyPinger(SlaveDao slaveDao, SlaveProxyFactory factory)
        {
            this.slaveDao = slaveDao;
            this.factory = factory;
        }

        public void run()
        {
            while (true)
            {
                try
                {
                    Thread.sleep(30000);
                }
                catch (InterruptedException e)
                {
                    e.printStackTrace();
                }

                for (Slave slave : slaveDao.findAll())
                {
                    try
                    {
                        SlaveService service = factory.createProxy(slave);
                        service.ping();
                        slave.lastPing(System.currentTimeMillis(), true);
                    }
                    catch (Exception e)
                    {
                        slave.lastPing(System.currentTimeMillis(), false);
                        LOG.severe("Unable to ping slave '" + slave.getName() + "'", e);
                    }

                    slaveDao.save(slave);
                }
            }
        }
    }
}
