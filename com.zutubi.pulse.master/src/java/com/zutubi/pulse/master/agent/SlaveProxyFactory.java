package com.zutubi.pulse.master.agent;

import com.zutubi.pulse.master.tove.config.agent.AgentConfiguration;
import com.zutubi.pulse.servercore.hessian.CustomHessianProxyFactory;
import com.zutubi.pulse.servercore.services.SlaveService;
import com.zutubi.pulse.servercore.services.UncontactableSlaveService;

import java.net.MalformedURLException;
import java.net.URL;

/**
 */
public class SlaveProxyFactory
{
    private CustomHessianProxyFactory hessianProxyFactory;

    public SlaveService createProxy(AgentConfiguration agentConfig)
    {
        return createProxy(agentConfig.getHost(), agentConfig.getPort());
    }

    public SlaveService createProxy(String host, int port)
    {
        try
        {
            return unsafeCreateProxy(host, port);
        }
        catch (MalformedURLException e)
        {
            return new UncontactableSlaveService(e.getMessage());
        }
    }

    public SlaveService unsafeCreateProxy(AgentConfiguration agentConfig) throws MalformedURLException
    {
        return unsafeCreateProxy(agentConfig.getHost(), agentConfig.getPort());
    }

    private SlaveService unsafeCreateProxy(String host, int port) throws MalformedURLException
    {
        URL url = new URL("http", host, port, "/hessian");
        return (SlaveService) hessianProxyFactory.create(SlaveService.class, url.toString());
    }

    public void setHessianProxyFactory(CustomHessianProxyFactory hessianProxyFactory)
    {
        this.hessianProxyFactory = hessianProxyFactory;
    }
}
