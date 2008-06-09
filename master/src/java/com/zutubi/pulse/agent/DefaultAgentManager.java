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
import com.zutubi.pulse.services.SlaveStatus;
import com.zutubi.pulse.services.UpgradeStatus;
import com.zutubi.pulse.util.logging.Logger;

import java.util.*;
import java.util.concurrent.locks.ReentrantLock;

/**
 */
public class DefaultAgentManager implements AgentManager, EventListener, Stoppable
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
                addSlaveAgent(slave, false);
            }
        }
        finally
        {
            lock.unlock();
        }
    }

    private void addSlaveAgent(Slave slave, boolean ping)
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

        SlaveAgent agent = new SlaveAgent(slave, service, serviceTokenManager, buildService);
        slaveAgents.put(slave.getId(), agent);

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

    public void enableSlave(long slaveId)
    {
        setSlaveState(slaveId, Slave.EnableState.ENABLED);
    }

    public void disableSlave(long slaveId)
    {
        setSlaveState(slaveId, Slave.EnableState.DISABLED);
    }

    public void setSlaveState(long slaveId, Slave.EnableState state)
    {
        Slave slave = slaveManager.getSlave(slaveId);
        slave.setEnableState(state);
        slaveManager.save(slave);
        slaveChanged(slave.getId());
    }

    private void pingSlave(SlaveAgent agent)
    {
        if (agent.isEnabled())
        {
            agentPingService.requestPing(agent, agent.getSlaveService());
        }
    }

    private void updateAgent(SlaveAgent agent)
    {
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

    private void handleAgentPing(SlaveAgent agent, SlaveStatus status)
    {
        Status oldStatus = agent.getStatus();
        agent.updateStatus(status);

        // If the slave has just come online, run resource
        // discovery
        if (status.getStatus().isOnline())
        {
            if (oldStatus.isOnline() && status.isFirst())
            {
                // The agent bounced between pings.  Act like we saw it by
                // sending the offline event and setting old status to
                // offline.
                agent.setStatus(Status.OFFLINE);
                eventManager.publish(new AgentStatusEvent(this, oldStatus, agent));
                oldStatus = Status.OFFLINE;
                agent.updateStatus(status);
            }

            if(!oldStatus.isOnline())
            {
                if(status.getRecipeId() != 0)
                {
                    // The agent appears to have just come online but is
                    // building.  Snap it out of that.
                    try
                    {
                        agent.getBuildService().terminateRecipe(status.getRecipeId());
                    }
                    catch (Exception e)
                    {
                        LOG.severe("Unable to terminate unwanted recipe on agent '" + agent.getName() + "': " + e.getMessage(), e);
                    }
                }

                try
                {
                    List<Resource> resources = agent.getSlaveService().discoverResources(serviceTokenManager.getToken());
                    resourceManager.addDiscoveredResources(slaveManager.getSlave(agent.getId()), resources);
                }
                catch (Exception e)
                {
                    LOG.warning("Unable to discover resource for agent '" + agent.getName() + "': " + e.getMessage(), e);
                }
            }
        }

        if (oldStatus != agent.getStatus())
        {
            eventManager.publish(new AgentStatusEvent(this, oldStatus, agent));

            if (status.getStatus() == Status.VERSION_MISMATCH)
            {
                // Try to update this agent.
                updateAgent(agent);
            }
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
            agent.setStatus(Status.OFFLINE);
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
        if(evt instanceof AgentPingEvent)
        {
            AgentPingEvent ape = (AgentPingEvent) evt;
            handleAgentPing((SlaveAgent) ape.getAgent(), ape.getPingStatus());
        }
        else
        {
            handleAgentUpgradeComplete(evt);
        }
    }

    public Class[] getHandledEvents()
    {
        return new Class[] { AgentPingEvent.class, SlaveUpgradeCompleteEvent.class };
    }

    public void setMasterLocationProvider(MasterLocationProvider masterLocationProvider)
    {
        this.masterLocationProvider = masterLocationProvider;
    }

    public void enableMasterAgent()
    {
        if (masterAgent.isEnabled())
        {
            // No need to go through all of the formalities. 
            return;
        }

        // generate a master agent status change event.
        AgentStatusEvent statusEvent = new AgentStatusEvent(this, masterAgent.getStatus(), masterAgent);

        masterAgent.setStatus(Status.IDLE);

        eventManager.publish(statusEvent);
    }

    public void disableMasterAgent()
    {
        if (!masterAgent.isEnabled())
        {
            // No need to go through all of the formalities.
            return;
        }

        // generate a master agent status change event.
        AgentStatusEvent statusEvent = new AgentStatusEvent(this, masterAgent.getStatus(), masterAgent);

        masterAgent.setStatus(Status.DISABLED);

        eventManager.publish(statusEvent);
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
        lock.lock();
        try
        {
            List<Agent> online = new LinkedList<Agent>();
            if (masterAgent.isEnabled())
            {
                online.add(masterAgent);
            }

            for (SlaveAgent a : slaveAgents.values())
            {
                if (a.isOnline())
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
                addSlaveAgent(slave, true);

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
                removeSlaveAgent(id);
                addSlaveAgent(slave, true);
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
