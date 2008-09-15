package com.zutubi.pulse;

import com.zutubi.pulse.agent.AgentManager;
import com.zutubi.pulse.spring.SpringComponentContext;
import com.zutubi.pulse.core.ResourceRepository;
import com.zutubi.pulse.core.config.Resource;
import com.zutubi.pulse.events.Event;
import com.zutubi.pulse.events.EventManager;
import com.zutubi.pulse.model.ResourceManager;
import com.zutubi.pulse.services.InvalidTokenException;
import com.zutubi.pulse.services.MasterService;
import com.zutubi.pulse.services.ServiceTokenManager;
import com.zutubi.pulse.services.UpgradeStatus;

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

    public Resource getResource(String token, long agentHandle, String name) throws InvalidTokenException
    {
        if (validateToken(token))
        {
            ResourceRepository repository = getResourceManager().getAgentRepository(agentHandle);
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
            ResourceRepository repository = getResourceManager().getAgentRepository(agentHandle);
            if (repository != null)
            {
                return repository.getResourceNames();
            }
        }

        return null;
    }

    private boolean validateToken(String token)
    {
        ServiceTokenManager tokenManager = getServiceTokenManager();
        if (tokenManager != null)
        {
            tokenManager.validateToken(token);
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
    }

    public void setResourceManager(ResourceManager resourceManager)
    {
        this.resourceManager = resourceManager;
    }

    public void setServiceTokenManager(ServiceTokenManager serviceTokenManager)
    {
        this.serviceTokenManager = serviceTokenManager;
    }

    private ResourceManager getResourceManager()
    {
        if(resourceManager == null)
        {
            SpringComponentContext.autowire(this);
        }
        return resourceManager;
    }

    public ServiceTokenManager getServiceTokenManager()
    {
        if(serviceTokenManager == null)
        {
            SpringComponentContext.autowire(this);
        }

        return serviceTokenManager;
    }

    public void setAgentManager(AgentManager agentManager)
    {
        this.agentManager = agentManager;
    }
}
