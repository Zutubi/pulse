package com.zutubi.pulse.agent;

import com.zutubi.pulse.MasterBuildService;
import com.zutubi.pulse.MasterRecipeProcessor;
import com.zutubi.pulse.SlaveBuildService;
import com.zutubi.pulse.SlaveProxyFactory;
import com.zutubi.pulse.bootstrap.MasterConfigurationManager;
import com.zutubi.pulse.bootstrap.StartupManager;
import com.zutubi.pulse.core.Stoppable;
import com.zutubi.pulse.core.model.Resource;
import com.zutubi.pulse.events.*;
import com.zutubi.pulse.events.EventListener;
import com.zutubi.pulse.license.LicenseException;
import com.zutubi.pulse.license.LicenseHolder;
import com.zutubi.pulse.license.LicenseManager;
import com.zutubi.pulse.license.authorisation.AddAgentAuthorisation;
import com.zutubi.pulse.logging.ServerMessagesHandler;
import com.zutubi.pulse.model.NamedEntityComparator;
import com.zutubi.pulse.model.ResourceManager;
import com.zutubi.pulse.model.Slave;
import com.zutubi.pulse.model.SlaveManager;
import com.zutubi.pulse.services.ServiceTokenManager;
import com.zutubi.pulse.services.SlaveService;
import com.zutubi.pulse.services.UpgradeStatus;
import com.zutubi.pulse.util.Predicate;
import com.zutubi.pulse.util.logging.Logger;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.locks.ReentrantLock;

/**
 */
public class DefaultAgentManager implements AgentManager, EventListener, Stoppable
{
    private static final Logger LOG = Logger.getLogger(DefaultAgentManager.class);

    private MasterAgent masterAgent;

    private ReentrantLock lock = new ReentrantLock();
    private Map<Long, SlaveAgent> slaveAgents;
    private AgentStatusManager agentStatusManager;

    private SlaveManager slaveManager;
    private MasterRecipeProcessor masterRecipeProcessor;
    private MasterConfigurationManager configurationManager;
    private ResourceManager resourceManager;
    private EventManager eventManager;
    private SlaveProxyFactory slaveProxyFactory;
    private StartupManager startupManager;
    private ServerMessagesHandler serverMessagesHandler;
    private ServiceTokenManager serviceTokenManager;
    private MasterLocationProvider masterLocationProvider;
    private AgentPingService agentPingService;

    private LicenseManager licenseManager;

    private Map<Long, AgentUpdater> updaters = new TreeMap<Long, AgentUpdater>();
    private ReentrantLock updatersLock = new ReentrantLock();

    public void init()
    {
        MasterBuildService masterService = new MasterBuildService(masterRecipeProcessor, masterLocationProvider, configurationManager.getUserPaths().getData(), resourceManager);
        masterAgent = new MasterAgent(masterService, configurationManager, startupManager, serverMessagesHandler);

        // Create this prior to refreshing the slaves so it can pick up the
        // slave added events.
        agentStatusManager = new AgentStatusManager(masterAgent, this, Executors.newSingleThreadExecutor(new ThreadFactory()
        {
            public Thread newThread(Runnable r)
            {
                Thread t = new Thread(r);
                t.setName("Agent Status Manager Event Pump");
                return t;
            }
        }), eventManager);

        refreshSlaveAgents();

        // register the canAddAgent license authorisation.
        AddAgentAuthorisation addAgentAuthorisation = new AddAgentAuthorisation();
        addAgentAuthorisation.setAgentManager(this);
        licenseManager.addAuthorisation(addAgentAuthorisation);

    }

    private void refreshSlaveAgents()
    {
        lock.lock();
        try
        {
            slaveAgents = new TreeMap<Long, SlaveAgent>();
            for (Slave slave : slaveManager.getAll())
            {
                addSlaveAgent(slave, false, false);
            }
        }
        finally
        {
            lock.unlock();
        }
    }

    private void addSlaveAgent(Slave slave, boolean ping, boolean changedExisting)
    {
        SlaveService service = slaveProxyFactory.createProxy(slave);
        SlaveBuildService buildService = new SlaveBuildService(service, serviceTokenManager, slave, masterLocationProvider, resourceManager);

        if (slave.getEnableState() == Slave.EnableState.UPGRADING)
        {
            // Something went wrong: lost contact with slave (or master
            // died) during upgrade.
            slave.setEnableState(Slave.EnableState.FAILED_UPGRADE);
            slaveManager.save(slave);
        }
        else if(slave.getEnableState() == Slave.EnableState.DISABLING)
        {
            slave.setEnableState(Slave.EnableState.DISABLED);
            slaveManager.save(slave);
        }
        
        SlaveAgent agent = new SlaveAgent(slave, service, serviceTokenManager, buildService);
        slaveAgents.put(slave.getId(), agent);
        if (changedExisting)
        {
            eventManager.publish(new AgentChangedEvent(this, agent));
        }
        else
        {
            eventManager.publish(new AgentAddedEvent(this, agent));
        }

        if (ping)
        {
            pingSlave(agent);
        }
    }

    public void pingSlaves()
    {
        Collection<SlaveAgent> copyOfSlaveAgents = null;

        lock.lock();
        try
        {
            copyOfSlaveAgents = new ArrayList<SlaveAgent>(slaveAgents.values());
        }
        finally
        {
            lock.unlock();
        }

        // pinging the slave agents may take a while, so lets do this outside the lock.
        for (SlaveAgent agent : copyOfSlaveAgents)
        {
            pingSlave(agent);
        }
    }

    public int getAgentCount()
    {
        return slaveAgents.size() + 1;
    }

    public void addSlave(Slave slave) throws LicenseException
    {
        LicenseHolder.ensureAuthorization(AddAgentAuthorisation.AUTH);

        slaveManager.save(slave);
        slaveAdded(slave.getId());
    }

    private void pingSlave(SlaveAgent agent)
    {
        if (agent.isEnabled())
        {
            agentPingService.requestPing(agent, agent.getSlaveService());
        }
    }

    public void stop(boolean force)
    {
        if (force)
        {
            updatersLock.lock();
            try
            {
                for (AgentUpdater updater : updaters.values())
                {
                    updater.stop(force);
                }
            }
            finally
            {
                updatersLock.unlock();
            }
        }
    }

    public void setEnableState(Agent agent, Slave.EnableState state)
    {
        if(agent.isSlave())
        {
            Slave slave = slaveManager.getSlave(agent.getId());
            slave.setEnableState(state);
            slaveManager.save(slave);
            ((SlaveAgent) agent).setSlave(slave);
        }
        else
        {
            configurationManager.getAppConfig().setMasterEnableState(state);
        }
    }

    private void handleAgentOnline(AgentOnlineEvent event)
    {
        Agent agent = event.getAgent();
        if (agent.isSlave())
        {
            try
            {
                List<Resource> resources = ((SlaveAgent) agent).getSlaveService().discoverResources(serviceTokenManager.getToken());
                resourceManager.addDiscoveredResources(slaveManager.getSlave(agent.getId()), resources);
            }
            catch (Exception e)
            {
                LOG.warning("Unable to discover resource for agent '" + agent.getName() + "': " + e.getMessage(), e);
            }
        }
    }

    private void handleUpgradeRequired(AgentUpgradeRequiredEvent event)
    {
        SlaveAgent agent = (SlaveAgent) event.getAgent();
        Slave slave = slaveManager.getSlave(agent.getId());
        slave.setEnableState(Slave.EnableState.UPGRADING);
        slaveManager.save(slave);
        agent.setSlave(slave);

        AgentUpdater updater = new AgentUpdater(agent, serviceTokenManager.getToken(), masterLocationProvider.getMasterUrl(), eventManager, configurationManager.getSystemPaths());
        updatersLock.lock();

        try
        {
            updaters.put(agent.getId(), updater);
            updater.start();
        }
        finally
        {
            updatersLock.unlock();
        }
    }

    private void handleAgentUpgradeComplete(Event evt)
    {
        SlaveUpgradeCompleteEvent suce = (SlaveUpgradeCompleteEvent) evt;
        SlaveAgent agent = suce.getSlaveAgent();
        Slave slave = slaveManager.getSlave(agent.getId());

        updatersLock.lock();
        try
        {
            updaters.remove(slave.getId());
        }
        finally
        {
            updatersLock.unlock();
        }

        if (suce.isSuccessful())
        {
            agent.updateStatus(Status.INITIAL);
            slave.setEnableState(Slave.EnableState.ENABLED);
            slaveManager.save(slave);
            agent.setSlave(slave);
            pingSlave(agent);
        }
        else
        {
            slave.setEnableState(Slave.EnableState.FAILED_UPGRADE);
            slaveManager.save(slave);
            agent.setSlave(slave);
        }
    }

    public void handleEvent(Event evt)
    {
        if (evt instanceof AgentPingRequestedEvent)
        {
            pingSlave(((AgentPingRequestedEvent) evt).getSlaveAgent());
        }
        else if (evt instanceof AgentOnlineEvent)
        {
            handleAgentOnline((AgentOnlineEvent)evt);
        }
        else if (evt instanceof AgentUpgradeRequiredEvent)
        {
            handleUpgradeRequired((AgentUpgradeRequiredEvent) evt);
        }
        else
        {
            handleAgentUpgradeComplete(evt);
        }
    }

    public Class[] getHandledEvents()
    {
        return new Class[] { AgentPingRequestedEvent.class, AgentOnlineEvent.class, AgentUpgradeRequiredEvent.class, SlaveUpgradeCompleteEvent.class };
    }

    public void setMasterLocationProvider(MasterLocationProvider masterLocationProvider)
    {
        this.masterLocationProvider = masterLocationProvider;
    }

    public List<Agent> getAllAgents()
    {
        lock.lock();
        try
        {
            List<Agent> result = new LinkedList<Agent>(slaveAgents.values());
            Collections.sort(result, new NamedEntityComparator());

            result.add(0, masterAgent);
            return result;
        }
        finally
        {
            lock.unlock();
        }
    }

    public List<Agent> getOnlineAgents()
    {
        return agentStatusManager.getAgentsByStatusPredicate(new Predicate<Status>()
        {
            public boolean satisfied(Status status)
            {
                return status.isOnline();
            }
        });
    }

    public List<Agent> getAvailableAgents()
    {
        return agentStatusManager.getAgentsByStatusPredicate(new Predicate<Status>()
        {
            public boolean satisfied(Status status)
            {
                return status.isOnline() && !status.isBusy();
            }
        });
    }

    public Agent getAgent(Slave slave)
    {
        if (slave == null)
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

    public void pingSlave(Slave slave)
    {
        pingSlave(slaveAgents.get(slave.getId()));
    }

    public void slaveAdded(long id)
    {
        Slave slave = slaveManager.getSlave(id);
        if (slave != null)
        {
            lock.lock();
            try
            {
                addSlaveAgent(slave, true, false);
                licenseManager.refreshAuthorisations();
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
        if (slave != null)
        {
            lock.lock();
            try
            {
                removeSlaveAgent(id, true);
                addSlaveAgent(slave, true, true);
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
            removeSlaveAgent(id, false);
            licenseManager.refreshAuthorisations();
        }
        finally
        {
            lock.unlock();
        }
    }

    public void upgradeStatus(UpgradeStatus upgradeStatus)
    {
        updatersLock.lock();
        try
        {
            AgentUpdater updater = updaters.get(upgradeStatus.getSlaveId());
            if (updater != null)
            {
                updater.upgradeStatus(upgradeStatus);
            }
            else
            {
                LOG.warning("Received upgrade status for agent that is not upgrading [" + upgradeStatus.getSlaveId() + "]");
            }
        }
        finally
        {
            updatersLock.unlock();
        }
    }

    public boolean agentExists(String name)
    {
        return getAgent(name) != null;
    }

    public Agent getAgent(String name)
    {
        if (masterAgent.getName().equals(name))
        {
            return masterAgent;
        }

        try // synchronize access to the slaveAgents map.
        {
            lock.lock();
            for (SlaveAgent s : slaveAgents.values())
            {
                if (s.getName().equals(name))
                {
                    return s;
                }
            }
        }
        finally
        {
            lock.unlock();
        }

        return null;
    }

    private void removeSlaveAgent(long id, boolean changedExisting)
    {
        SlaveAgent agent = slaveAgents.remove(id);
        if (!changedExisting)
        {
            eventManager.publish(new AgentRemovedEvent(this, agent));
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
        eventManager.register(this);
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

    public void setLicenseManager(LicenseManager licenseManager)
    {
        this.licenseManager = licenseManager;
    }

    public void setAgentPingService(AgentPingService agentPingService)
    {
        this.agentPingService = agentPingService;
    }
}
