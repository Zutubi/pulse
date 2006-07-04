package com.zutubi.pulse.agent;

import com.zutubi.pulse.*;
import com.zutubi.pulse.bootstrap.MasterConfigurationManager;
import com.zutubi.pulse.bootstrap.StartupManager;
import com.zutubi.pulse.core.model.Resource;
import com.zutubi.pulse.events.EventManager;
import com.zutubi.pulse.events.SlaveAgentRemovedEvent;
import com.zutubi.pulse.events.SlaveStatusEvent;
import com.zutubi.pulse.logging.ServerMessagesHandler;
import com.zutubi.pulse.model.NamedEntityComparator;
import com.zutubi.pulse.model.ResourceManager;
import com.zutubi.pulse.model.Slave;
import com.zutubi.pulse.model.SlaveManager;
import com.zutubi.pulse.services.ServiceTokenManager;
import com.zutubi.pulse.services.SlaveService;
import com.zutubi.pulse.services.SlaveStatus;
import com.zutubi.pulse.util.logging.Logger;

import java.net.MalformedURLException;
import java.util.*;
import java.util.concurrent.locks.ReentrantLock;

/**
 */
public class DefaultAgentManager implements AgentManager
{
    private static final Logger LOG = Logger.getLogger(DefaultAgentManager.class);

    private final int masterBuildNumber = Version.getVersion().getBuildNumberAsInt();

    private MasterAgent masterAgent;

    private ReentrantLock lock = new ReentrantLock();
    private Map<Long, SlaveAgent> slaveAgents;

    private SlaveManager slaveManager;
    private MasterRecipeProcessor masterRecipeProcessor;
    private MasterConfigurationManager configurationManager;
    private ResourceManager resourceManager;
    private EventManager eventManager;
    private SlaveProxyFactory slaveProxyFactory;
    private StartupManager startupManager;
    private ServerMessagesHandler serverMessagesHandler;
    private ServiceTokenManager serviceTokenManager;

    public void init()
    {
        MasterBuildService masterService = new MasterBuildService(masterRecipeProcessor, configurationManager, resourceManager);
        masterAgent = new MasterAgent(masterService,configurationManager, startupManager, serverMessagesHandler);

        refreshSlaveAgents();
    }

    private void refreshSlaveAgents()
    {
        lock.lock();
        try
        {
            slaveAgents = new TreeMap<Long, SlaveAgent>();
            for (Slave slave : slaveManager.getAll())
            {
                SlaveAgent agent = addSlaveAgent(slave);
                if(agent != null)
                {
                    pingSlave(agent);
                }
            }
        }
        finally
        {
            lock.unlock();
        }
    }

    private SlaveAgent addSlaveAgent(Slave slave)
    {
        try
        {
            SlaveService service = slaveProxyFactory.createProxy(slave);
            SlaveBuildService buildService = new SlaveBuildService(service, serviceTokenManager, slave, configurationManager, resourceManager);
            SlaveAgent agent = new SlaveAgent(slave, service, serviceTokenManager, buildService);
            slaveAgents.put(slave.getId(), agent);
            return agent;
        }
        catch (MalformedURLException e)
        {
            LOG.severe("Unable to contact slave '" + slave.getName() + "': " + e.getMessage());
            return null;
        }
    }

    public void pingSlaves()
    {
        lock.lock();
        try
        {
            for (SlaveAgent agent: slaveAgents.values())
            {
                pingSlave(agent);
            }
        }
        finally
        {
            lock.unlock();
        }
    }

    public int getAgentCount()
    {
        return slaveAgents.size() + 1;
    }

    private void pingSlave(SlaveAgent agent)
    {
        if (agent.isEnabled())
        {
            long currentTime = System.currentTimeMillis();
            Status oldStatus = agent.getStatus();

            try
            {
                int build = agent.getSlaveService().ping();
                if(build == masterBuildNumber)
                {
                    String token = serviceTokenManager.getToken();
                    SlaveStatus status = agent.getSlaveService().getStatus(token);
                    agent.pinged(currentTime, status);

                    // If the slave has just come online, run resource
                    // discovery
                    if(!oldStatus.isOnline() && status.getStatus().isOnline())
                    {
                        List<Resource> resources = agent.getSlaveService().discoverResources(token);
                        resourceManager.addDiscoveredResources(agent.getSlave(), resources);
                    }
                }
                else
                {
                    agent.versionMismatch(currentTime);
                }
            }
            catch (Exception e)
            {
                agent.failedPing(currentTime, "Exception: '" + e.getClass().getName() + "'. Reason: " + e.getMessage());
            }

            if(oldStatus != agent.getStatus())
            {
                eventManager.publish(new SlaveStatusEvent(this, oldStatus, agent));
            }
        }
    }

    public List<Agent> getAllAgents()
    {
        List<Agent> result = new LinkedList<Agent>(slaveAgents.values());
        Collections.sort(result, new NamedEntityComparator());

        result.add(0, masterAgent);
        return result;
    }

    public List<Agent> getOnlineAgents()
    {
        lock.lock();
        try
        {
            List<Agent> online = new LinkedList<Agent>();
            online.add(masterAgent);

            for(SlaveAgent a: slaveAgents.values())
            {
                if(a.isOnline())
                {
                    online.add(a);
                }
            }

            return online;
        }
        finally
        {
            lock.unlock();
        }
    }

    public Agent getAgent(Slave slave)
    {
        if(slave == null)
        {
            return masterAgent;
        }
        else
        {
            lock.lock();
            try
            {
                return slaveAgents.get(slave.getId());
            }
            finally
            {
                lock.unlock();
            }
        }
    }

    public void slaveAdded(long id)
    {
        Slave slave = slaveManager.getSlave(id);
        if(slave != null)
        {
            lock.lock();
            try
            {
                addSlaveAgent(slave);
            }
            finally
            {
                lock.unlock();
            }
        }
    }

    public void slaveChanged(long id)
    {
        Slave slave = slaveManager.getSlave(id);
        if(slave != null)
        {
            lock.lock();
            try
            {
                removeSlaveAgent(id);
                addSlaveAgent(slave);
            }
            finally
            {
                lock.unlock();
            }
        }
    }

    public void slaveDeleted(long id)
    {
        lock.lock();
        try
        {
            removeSlaveAgent(id);
        }
        finally
        {
            lock.unlock();
        }
    }

    private void removeSlaveAgent(long id)
    {
        SlaveAgent agent = slaveAgents.remove(id);
        eventManager.publish(new SlaveAgentRemovedEvent(this, agent));
    }

    public void setSlaveManager(SlaveManager slaveManager)
    {
        this.slaveManager = slaveManager;
    }

    public void setMasterRecipeProcessor(MasterRecipeProcessor masterRecipeProcessor)
    {
        this.masterRecipeProcessor = masterRecipeProcessor;
    }

    public void setConfigurationManager(MasterConfigurationManager configurationManager)
    {
        this.configurationManager = configurationManager;
    }

    public void setResourceManager(ResourceManager resourceManager)
    {
        this.resourceManager = resourceManager;
    }

    public void setSlaveProxyFactory(SlaveProxyFactory slaveProxyFactory)
    {
        this.slaveProxyFactory = slaveProxyFactory;
    }

    public void setEventManager(EventManager eventManager)
    {
        this.eventManager = eventManager;
    }

    public void setStartupManager(StartupManager startupManager)
    {
        this.startupManager = startupManager;
    }

    public void setServerMessagesHandler(ServerMessagesHandler serverMessagesHandler)
    {
        this.serverMessagesHandler = serverMessagesHandler;
    }

    public void setServiceTokenManager(ServiceTokenManager serviceTokenManager)
    {
        this.serviceTokenManager = serviceTokenManager;
    }
}
