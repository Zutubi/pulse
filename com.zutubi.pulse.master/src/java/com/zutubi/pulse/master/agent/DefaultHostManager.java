package com.zutubi.pulse.master.agent;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.zutubi.pulse.master.model.HostState;
import com.zutubi.pulse.master.model.persistence.EntityDao;
import com.zutubi.pulse.master.tove.config.agent.AgentConfiguration;
import com.zutubi.pulse.servercore.services.SlaveService;
import com.zutubi.util.bean.ObjectFactory;

import java.util.*;

import static com.google.common.collect.Iterables.any;
import static com.google.common.collect.Iterables.filter;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Lists.transform;

/**
 * Manages hosts where agents are located, sharing a single host among agents
 * that are configured at the same location.
 */
public class DefaultHostManager implements HostManager
{
    private final Map<String, Host> locationToHostMap = new HashMap<String, Host>();
    private final Map<Long, Host> agentHandleToHostMap = new HashMap<Long, Host>();
    private final Map<Host, List<Long>> hostToAgentHandlesMap = new HashMap<Host, List<Long>>();
    private final Map<Host, HostService> hostToServiceMap = new HashMap<Host, HostService>();

    private AgentManager agentManager;
    private EntityDao<HostState> hostStateDao;
    private ObjectFactory objectFactory;
    private SlaveProxyFactory slaveProxyFactory;
    private HostPingService hostPingService;

    public void init(AgentManager agentManager)
    {
        this.agentManager = agentManager;

        List<HostState> hostStates = hostStateDao.findAll();
        synchronized (locationToHostMap)
        {
            for (HostState state: hostStates)
            {
                if (state.getUpgradeState() == HostState.PersistentUpgradeState.UPGRADING)
                {
                    state.setUpgradeState(HostState.PersistentUpgradeState.FAILED_UPGRADE);
                    hostStateDao.save(state);
                }

                Host host = buildHost(state);
                locationToHostMap.put(host.getLocation(), host);
            }
        }
    }

    public Host agentAdded(AgentConfiguration agentConfig)
    {
        String location = HostLocationFormatter.format(agentConfig);
        synchronized (locationToHostMap)
        {
            Host host = locationToHostMap.get(location);
            if (host == null)
            {
                host = buildHost(createState(agentConfig));
                locationToHostMap.put(location, host);
            }

            addToHostAgentMaps(host, agentConfig.getHandle());
            return host;
        }
    }

    private void addToHostAgentMaps(Host host, long agentHandle)
    {
        agentHandleToHostMap.put(agentHandle, host);
        List<Long> agentIds = hostToAgentHandlesMap.get(host);
        if (agentIds == null)
        {
            agentIds = new LinkedList<Long>();
            hostToAgentHandlesMap.put(host, agentIds);
        }

        agentIds.add(agentHandle);
    }

    private Host removeFromHostAgentMaps(long agentHandle)
    {
        Host host = agentHandleToHostMap.remove(agentHandle);
        if (host != null)
        {
            List<Long> agentIds = hostToAgentHandlesMap.get(host);
            agentIds.remove(agentHandle);
        }

        return host;
    }

    private DefaultHost buildHost(HostState hostState)
    {
        return objectFactory.buildBean(DefaultHost.class, hostState);
    }

    private HostState createState(AgentConfiguration agentConfig)
    {
        HostState newState = agentConfig.isRemote() ? new HostState(agentConfig.getHost(), agentConfig.getPort(), agentConfig.isSsl()) : new HostState();
        hostStateDao.save(newState);
        return newState;
    }

    public Host agentChanged(AgentConfiguration agentConfig)
    {
        synchronized (locationToHostMap)
        {
            Host host = getHostForAgent(agentConfig);
            if (!host.getLocation().equals(HostLocationFormatter.format(agentConfig)))
            {
                // Agent now requires a different host, remove and add it.
                agentDeleted(agentConfig.getHandle());
                agentAdded(agentConfig);
                host = getHostForAgent(agentConfig);
            }

            return host;
        }
    }

    public void agentDeleted(AgentConfiguration agentConfig)
    {
        agentDeleted(agentConfig.getHandle());
    }

    private void agentDeleted(long agentHandle)
    {
        synchronized (locationToHostMap)
        {
            Host host = removeFromHostAgentMaps(agentHandle);
            if (host != null && hostHasNoAgents(host))
            {
                // This was the last agent using the host, clean it up.
                locationToHostMap.remove(host.getLocation());
                HostState hostState = hostStateDao.findById(host.getId());
                hostStateDao.delete(hostState);
            }
        }
    }

    private boolean hostHasNoAgents(Host host)
    {
        List<Long> agentIds = hostToAgentHandlesMap.get(host);
        return agentIds == null || agentIds.isEmpty();
    }

    public Host getHostForLocation(String location)
    {
        synchronized (locationToHostMap)
        {
            return locationToHostMap.get(location);
        }
    }

    public Host getHostForAgent(AgentConfiguration agentConfig)
    {
        return agentHandleToHostMap.get(agentConfig.getHandle());
    }

    public Collection<Agent> getAgentsForHost(Host host)
    {
        List<Long> agentIds = hostToAgentHandlesMap.get(host);
        if (agentIds == null)
        {
            return Collections.emptyList();
        }

        List<Agent> agents = transform(agentIds, new Function<Long, Agent>()
        {
            public Agent apply(Long agentHandle)
            {
                return agentManager.getAgentByHandle(agentHandle);
            }
        });

        return newArrayList(filter(agents, Predicates.notNull()));
    }

    public HostService getServiceForHost(Host host)
    {
        synchronized (hostToServiceMap)
        {
            HostService service = hostToServiceMap.get(host);
            if (service == null)
            {
                service = createService(host);
                hostToServiceMap.put(host, service);
            }

            return service;
        }
    }

    public void pingHosts()
    {
        Collection<Host> copyOfHosts;
        synchronized (locationToHostMap)
        {
            copyOfHosts = new ArrayList<Host>(locationToHostMap.values());
        }

        // Pinging may take a while, so lets do this outside the lock.
        for (Host host: copyOfHosts)
        {
            pingHost(host);
        }
    }

    public void pingHost(Host host)
    {
        if (!host.isUpgrading())
        {
            boolean hasEnabledAgent = any(getAgentsForHost(host), new Predicate<Agent>()
            {
                public boolean apply(Agent agent)
                {
                    return agent.isEnabled();
                }
            });

            if (hasEnabledAgent)
            {
                hostPingService.requestPing(host, getServiceForHost(host));
            }
        }
    }

    private HostService createService(Host host)
    {
        if (host.isRemote())
        {
            SlaveService slaveService = slaveProxyFactory.createProxy(host.getHostName(), host.getPort(), host.isSsl());
            return objectFactory.buildBean(SlaveHostService.class, slaveService);
        }
        else
        {
            return objectFactory.buildBean(MasterHostService.class);
        }
    }

    public void setHostStateDao(EntityDao<HostState> hostStateDao)
    {
        this.hostStateDao = hostStateDao;
    }

    public void setObjectFactory(ObjectFactory objectFactory)
    {
        this.objectFactory = objectFactory;
    }

    public void setSlaveProxyFactory(SlaveProxyFactory slaveProxyFactory)
    {
        this.slaveProxyFactory = slaveProxyFactory;
    }

    public void setHostPingService(HostPingService hostPingService)
    {
        this.hostPingService = hostPingService;
    }
}
