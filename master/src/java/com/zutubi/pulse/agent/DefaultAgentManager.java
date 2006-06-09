package com.zutubi.pulse.agent;

import com.zutubi.pulse.MasterBuildService;
import com.zutubi.pulse.MasterRecipeProcessor;
import com.zutubi.pulse.SlaveBuildService;
import com.zutubi.pulse.SlaveProxyFactory;
import com.zutubi.pulse.bootstrap.MasterConfigurationManager;
import com.zutubi.pulse.bootstrap.StartupManager;
import com.zutubi.pulse.events.EventManager;
import com.zutubi.pulse.events.SlaveAvailableEvent;
import com.zutubi.pulse.events.SlaveUnavailableEvent;
import com.zutubi.pulse.logging.ServerMessagesHandler;
import com.zutubi.pulse.model.ResourceManager;
import com.zutubi.pulse.model.Slave;
import com.zutubi.pulse.model.SlaveManager;
import com.zutubi.pulse.services.SlaveService;
import com.zutubi.pulse.util.Sort;
import com.zutubi.pulse.util.logging.Logger;

import java.net.MalformedURLException;
import java.util.*;
import java.util.concurrent.locks.ReentrantLock;

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
    private MasterConfigurationManager configurationManager;
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

    private void pingSlave(SlaveAgent agent)
    {
        long currentTime = System.currentTimeMillis();
        Status oldStatus = agent.getStatus();

        try
        {
            int build = agent.getSlaveService().ping();
            agent.pinged(currentTime, build);
        }
        catch (Exception e)
        {
            agent.failedPing(currentTime, "Exception: '" + e.getClass().getName() + "'. Reason: " + e.getMessage());
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

    public void slaveDeleted(long id)
    {
        lock.lock();
        try
        {
            SlaveAgent agent = slaveAgents.remove(id);
            eventManager.publish(new SlaveUnavailableEvent(this, agent.getSlave()));
        }
        finally
        {
            lock.unlock();
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
}
