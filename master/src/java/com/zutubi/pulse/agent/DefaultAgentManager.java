package com.zutubi.pulse.agent;

import com.zutubi.prototype.config.ConfigurationProvider;
import com.zutubi.pulse.*;
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
import com.zutubi.pulse.prototype.config.admin.GeneralAdminConfiguration;
import com.zutubi.pulse.services.ServiceTokenManager;
import com.zutubi.pulse.services.SlaveService;
import com.zutubi.pulse.services.SlaveStatus;
import com.zutubi.pulse.services.UpgradeStatus;
import com.zutubi.util.logging.Logger;

import java.net.ConnectException;
import java.net.MalformedURLException;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.locks.ReentrantLock;

/**
 */
public class DefaultAgentManager implements AgentManager, EventListener, Stoppable
{
    private static final Logger LOG = Logger.getLogger(DefaultAgentManager.class);

    private final int masterBuildNumber = Version.getVersion().getBuildNumberAsInt();

    private MasterAgent masterAgent;

    private ReentrantLock lock = new ReentrantLock();
    private Map<Long, SlaveAgent> slaveAgents;

    private SlaveManager slaveManager;
    private MasterRecipeProcessor masterRecipeProcessor;
    private MasterConfigurationManager configurationManager;
    private ConfigurationProvider configurationProvider;
    private ResourceManager resourceManager;
    private EventManager eventManager;
    private SlaveProxyFactory slaveProxyFactory;
    private StartupManager startupManager;
    private ServerMessagesHandler serverMessagesHandler;
    private ServiceTokenManager serviceTokenManager;

    private LicenseManager licenseManager;
    private ExecutorService pingService = Executors.newCachedThreadPool();

    private Map<Long, AgentUpdater> updaters = new TreeMap<Long, AgentUpdater>();
    private ReentrantLock updatersLock = new ReentrantLock();

    // ping timeout in seconds.
    private static final int PING_TIMEOUT = Integer.getInteger("pulse.agent.ping.timeout", 45);

    public void init()
    {
        MasterBuildService masterService = new MasterBuildService(masterRecipeProcessor, configurationProvider, configurationManager, resourceManager);
        masterAgent = new MasterAgent(masterService, configurationProvider, configurationManager, startupManager, serverMessagesHandler);

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
        try
        {
            SlaveService service = slaveProxyFactory.createProxy(slave);
            SlaveBuildService buildService = new SlaveBuildService(service, serviceTokenManager, slave, configurationProvider, configurationManager, resourceManager);

            if(slave.getEnableState() == Slave.EnableState.UPGRADING)
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
        catch (MalformedURLException e)
        {
            LOG.severe("Unable to contact slave '" + slave.getName() + "': " + e.getMessage());
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
        for (SlaveAgent agent: copyOfSlaveAgents)
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

    public void enableSlave(Slave slave)
    {
        setSlaveState(slave, Slave.EnableState.ENABLED);
    }

    public void disableSlave(Slave slave)
    {
        setSlaveState(slave, Slave.EnableState.DISABLED);
    }

    public void setSlaveState(Slave slave, Slave.EnableState state)
    {
        slave.setEnableState(state);
        slaveManager.save(slave);
        slaveChanged(slave.getId());
    }

    private void pingSlave(SlaveAgent agent)
    {
        if (agent.isEnabled())
        {
            Status oldStatus = agent.getStatus();

            FutureTask<SlaveStatus> future = new FutureTask<SlaveStatus>(new Pinger(agent));
            pingService.execute(future);

            SlaveStatus status;

            try
            {
                status = future.get(PING_TIMEOUT, TimeUnit.SECONDS);
            }
            catch (TimeoutException e)
            {
                LOG.warning("Timed out pinging agent '" + agent.getName() + "'", e);
                status = new SlaveStatus(Status.OFFLINE, "Agent ping timed out");
            }
            catch(Exception e)
            {
                String message = "Unexpected error pinging agent '" + agent.getName() + "': " + e.getMessage();
                LOG.warning(message);
                LOG.debug(e);
                status = new SlaveStatus(Status.OFFLINE, message);
            }

            agent.updateStatus(status);

            // If the slave has just come online, run resource
            // discovery
            if(!oldStatus.isOnline() && status.getStatus().isOnline())
            {
                try
                {
                    List<Resource> resources = agent.getSlaveService().discoverResources(serviceTokenManager.getToken());
                    resourceManager.addDiscoveredResources(agent.getSlave(), resources);
                }
                catch (Exception e)
                {
                    LOG.warning("Unable to discover resource for agent '" + agent.getName() + "': " + e.getMessage(), e);
                }
            }

            if(oldStatus != agent.getStatus())
            {
                eventManager.publish(new AgentStatusEvent(this, oldStatus, agent));

                if(status.getStatus() == Status.VERSION_MISMATCH)
                {
                    // Try to update this agent.
                    updateAgent(agent);
                }
            }
        }
    }

    private void updateAgent(SlaveAgent agent)
    {
        Slave slave = agent.getSlave();
        slave.setEnableState(Slave.EnableState.UPGRADING);
        slaveManager.save(slave);

        String masterUrl = "http://" + MasterAgent.constructMasterLocation(configurationProvider.get(GeneralAdminConfiguration.class), configurationManager.getSystemConfig());
        AgentUpdater updater = new AgentUpdater(agent, serviceTokenManager.getToken(), masterUrl, eventManager, configurationManager.getSystemPaths());
        updatersLock.lock();

        try
        {
            updaters.put(agent.getSlave().getId(), updater);
            updater.start();
        }
        finally
        {
            updatersLock.unlock();
        }
    }

    public void stop(boolean force)
    {
        if(force)
        {
            pingService.shutdownNow();
            updatersLock.lock();
            try
            {
                for(AgentUpdater updater: updaters.values())
                {
                    updater.stop(force);
                }
            }
            finally
            {
                updatersLock.unlock();
            }
        }
        else
        {
            pingService.shutdown();
        }
    }

    public void handleEvent(Event evt)
    {
        SlaveUpgradeCompleteEvent suce = (SlaveUpgradeCompleteEvent) evt;
        Slave slave = suce.getSlaveAgent().getSlave();

        updatersLock.lock();
        try
        {
            updaters.remove(slave.getId());
        }
        finally
        {
            updatersLock.unlock();
        }

        if(suce.isSuccessful())
        {
            suce.getSlaveAgent().setStatus(Status.OFFLINE);
            slave.setEnableState(Slave.EnableState.ENABLED);
            slaveManager.save(slave);
            pingSlave(suce.getSlaveAgent());
        }
        else
        {
            slave.setEnableState(Slave.EnableState.FAILED_UPGRADE);
            slaveManager.save(slave);
        }
    }

    public Class[] getHandledEvents()
    {
        return new Class[] { SlaveUpgradeCompleteEvent.class };
    }

    public void setConfigurationProvider(ConfigurationProvider configurationProvider)
    {
        this.configurationProvider = configurationProvider;
    }

    private class Pinger implements Callable<SlaveStatus>
    {
        private SlaveAgent agent;

        public Pinger(SlaveAgent agent)
        {
            this.agent = agent;
        }

        public SlaveStatus call() throws Exception
        {
            SlaveStatus status;

            try
            {
                int build = agent.getSlaveService().ping();
                if(build == masterBuildNumber)
                {
                    String token = serviceTokenManager.getToken();
                    status = agent.getSlaveService().getStatus(token, "http://" + MasterAgent.constructMasterLocation(configurationProvider.get(GeneralAdminConfiguration.class), configurationManager.getSystemConfig()));
                }
                else
                {
                    status = new SlaveStatus(Status.VERSION_MISMATCH);
                }
            }
            catch (Exception e)
            {
                Throwable cause = e.getCause();
                // the most common cause of the exception is the Connect Exception.
                if (cause instanceof ConnectException)
                {
                    status = new SlaveStatus(Status.OFFLINE, cause.getMessage());
                }
                else
                {
                    LOG.warning("Exception pinging agent '" + agent.getName() + "': " + e.getMessage());
                    LOG.debug(e);
                    status = new SlaveStatus(Status.OFFLINE, "Exception: '" + e.getClass().getName() + "'. Reason: " + e.getMessage());
                }
            }

            status.setPingTime(System.currentTimeMillis());
            return status;
        }
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

    public void pingSlave(Slave slave)
    {
        pingSlave(slaveAgents.get(slave.getId()));
    }

    public void slaveAdded(long id)
    {
        Slave slave = slaveManager.getSlave(id);
        if(slave != null)
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
        if(slave != null)
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
            if(updater != null)
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
        if(masterAgent.getName().equals(name))
        {
            return masterAgent;
        }

        try // synchronize access to the slaveAgents map.
        {
            lock.lock();
            for(SlaveAgent s: slaveAgents.values())
            {
                if(s.getName().equals(name))
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
}
