package com.zutubi.pulse.master;

import com.zutubi.events.Event;
import com.zutubi.events.EventManager;
import com.zutubi.pulse.core.ResourceRepository;
import com.zutubi.pulse.core.config.ResourceConfiguration;
import com.zutubi.pulse.core.spring.SpringComponentContext;
import com.zutubi.pulse.master.agent.AgentManager;
import com.zutubi.pulse.master.model.ResourceManager;
import com.zutubi.pulse.servercore.events.system.SystemStartedListener;
import com.zutubi.pulse.servercore.services.InvalidTokenException;
import com.zutubi.pulse.servercore.services.MasterService;
import com.zutubi.pulse.servercore.services.ServiceTokenManager;
import com.zutubi.pulse.servercore.services.UpgradeStatus;

import java.util.List;

/**
 */
public class MasterServiceImpl implements MasterService
{
    private ServiceTokenManager serviceTokenManager;
    private EventManager eventManager;
    private ResourceManager resourceManager;
    private AgentManager agentManager;

    public void pong()
    {
        // Empty: just to test coms.
    }

    public void upgradeStatus(String token, UpgradeStatus upgradeStatus)
    {
        if (validateToken(token))
        {
            agentManager.upgradeStatus(upgradeStatus);
        }
    }

    public void handleEvent(String token, Event event) throws InvalidTokenException
    {
        if (validateToken(token))
        {
            eventManager.publish(event);
        }
    }

    public ResourceConfiguration getResource(String token, long agentHandle, String name) throws InvalidTokenException
    {
        if (validateToken(token))
        {
            ResourceRepository repository = resourceManager.getAgentRepository(agentHandle);
            if (repository != null)
            {
                return repository.getResource(name);
            }
        }

        return null;
    }

    public List<String> getResourceNames(String token, long agentHandle) throws InvalidTokenException
    {
        if (validateToken(token))
        {
            ResourceRepository repository = resourceManager.getAgentRepository(agentHandle);
            if (repository != null)
            {
                return repository.getResourceNames();
            }
        }

        return null;
    }

    private boolean validateToken(String token)
    {
        if (serviceTokenManager != null)
        {
            serviceTokenManager.validateToken(token);
            return true;
        }
        else
        {
            // CIB-1019: agents can send messages during master startup
            return false;
        }
    }

    public void setEventManager(EventManager eventManager)
    {
        this.eventManager = eventManager;
        eventManager.register(new SystemStartedListener()
        {
            public void systemStarted()
            {
                SpringComponentContext.autowire(MasterServiceImpl.this);
            }
        });
    }

    public void setResourceManager(ResourceManager resourceManager)
    {
        this.resourceManager = resourceManager;
    }

    public void setServiceTokenManager(ServiceTokenManager serviceTokenManager)
    {
        this.serviceTokenManager = serviceTokenManager;
    }

    public void setAgentManager(AgentManager agentManager)
    {
        this.agentManager = agentManager;
    }
}
