package com.zutubi.pulse.agent;

import com.zutubi.pulse.model.Slave;
import com.zutubi.pulse.model.SlaveManager;
import com.zutubi.pulse.model.ResourceManager;
import com.zutubi.pulse.MasterBuildService;
import com.zutubi.pulse.MasterRecipeProcessor;
import com.zutubi.pulse.SlaveProxyFactory;
import com.zutubi.pulse.SlaveBuildService;
import com.zutubi.pulse.logging.ServerMessagesHandler;
import com.zutubi.pulse.util.logging.Logger;
import com.zutubi.pulse.util.Sort;
import com.zutubi.pulse.events.SlaveAvailableEvent;
import com.zutubi.pulse.events.EventManager;
import com.zutubi.pulse.events.SlaveUnavailableEvent;
import com.zutubi.pulse.services.SlaveService;
import com.zutubi.pulse.bootstrap.ConfigurationManager;
import com.zutubi.pulse.bootstrap.StartupManager;

import java.util.*;
import java.util.concurrent.locks.ReentrantLock;
import java.net.MalformedURLException;

/**
 */
public class DefaultAgentManager implements AgentManager
{
    private static final Logger LOG = Logger.getLogger(DefaultAgentManager.class);

    private MasterAgent masterAgent;

    private ReentrantLock lock = new ReentrantLock();
    private Map<Long, SlaveAgent> slaveAgents;

    private SlaveManager slaveManager;
    private MasterRecipeProcessor masterRecipeProcessor;
    private ConfigurationManager configurationManager;
    private ResourceManager resourceManager;
    private EventManager eventManager;
    private SlaveProxyFactory slaveProxyFactory;
    private StartupManager startupManager;
    private ServerMessagesHandler serverMessagesHandler;

    public void init()
    {
        MasterBuildService masterService = new MasterBuildService(masterRecipeProcessor, configurationManager, resourceManager);
        masterAgent = new MasterAgent(masterService,configurationManager, startupManager, serverMessagesHandler);

        refreshSlaveAgents();
    }

    private void refreshSlaveAgents()
    {
        try
        {
            lock.lock();
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
            SlaveAgent agent = new SlaveAgent(slave, service, new SlaveBuildService(service, slave, configurationManager, resourceManager));
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
        try
        {
            lock.lock();
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

    private void pingSlave(SlaveAgent agent)
    {
        long currentTime = System.currentTimeMillis();
        Status oldStatus = agent.getStatus();

        try
        {
            agent.getSlaveService().ping();
            agent.pinged(currentTime, true);
        }
        catch (Exception e)
        {
            LOG.warning("Ping to slave '" + agent.getSlave().getName() + "' failed. Exception: '" + e.getClass().getName() + "'. Reason: " + e.getMessage());
            agent.pinged(currentTime, false);
        }

        int newOrdinal = agent.getStatus().ordinal();
        int oldOrdinal = oldStatus.ordinal();
        if(newOrdinal > oldOrdinal)
        {
            eventManager.publish(new SlaveAvailableEvent(this, agent.getSlave()));
        }
        else if(newOrdinal < oldOrdinal)
        {
            eventManager.publish(new SlaveUnavailableEvent(this, agent.getSlave()));
        }

    }

    public List<Agent> getAllAgents()
    {
        List<Agent> result = new LinkedList<Agent>(slaveAgents.values());
        final Comparator<String> comparator = new Sort.StringComparator();
        Collections.sort(result, new Comparator<Agent>()
        {
            public int compare(Agent a1, Agent a2)
            {
                return comparator.compare(a1.getName(), a2.getName());
            }
        });

        result.add(0, masterAgent);
        return result;
    }

    public List<Agent> getOnlineAgents()
    {
        try
        {
            lock.lock();

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
            try
            {
                lock.lock();
                return slaveAgents.get(slave.getId());
            }
            finally
            {
                lock.unlock();
            }
        }
    }

    public void newSlave(long id)
    {
        Slave slave = slaveManager.getSlave(id);
        if(slave != null)
        {
            try
            {
                lock.lock();
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
            try
            {
                lock.lock();
                SlaveAgent agent = slaveAgents.remove(id);
                eventManager.publish(new SlaveUnavailableEvent(this, agent.getSlave()));
                addSlaveAgent(slave);
            }
            finally
            {
                lock.unlock();
            }
        }
    }

    public void setSlaveManager(SlaveManager slaveManager)
    {
        this.slaveManager = slaveManager;
    }

    public void setMasterRecipeProcessor(MasterRecipeProcessor masterRecipeProcessor)
    {
        this.masterRecipeProcessor = masterRecipeProcessor;
    }

    public void setConfigurationManager(ConfigurationManager configurationManager)
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
}
