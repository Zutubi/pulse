package com.zutubi.pulse.prototype.config.agent;

import com.zutubi.config.annotations.SymbolicName;
import com.zutubi.config.annotations.Internal;
import com.zutubi.config.annotations.Form;
import com.zutubi.pulse.core.config.AbstractNamedConfiguration;
import com.zutubi.pulse.core.config.Resource;
import com.zutubi.validation.annotations.Numeric;

import java.util.Map;

/**
 */
@Form(fieldOrder = {"name", "remote", "host", "port"})
@SymbolicName("internal.agentConfig")
public class AgentConfiguration extends AbstractNamedConfiguration
{
    @Internal
    private long agentId;
    private boolean remote = true;
    private String host;
    @Numeric(min = 1)
    private int port;
    private Map<String, Resource> resources;

    public boolean isRemote()
    {
        return remote;
    }

    public void setRemote(boolean remote)
    {
        this.remote = remote;
    }

    public String getHost()
    {
        return host;
    }

    public void setHost(String host)
    {
        this.host = host;
    }

    public int getPort()
    {
        return port;
    }

    public void setPort(int port)
    {
        this.port = port;
    }

    public long getAgentId()
    {
        return agentId;
    }

    public void setAgentId(long agentId)
    {
        this.agentId = agentId;
    }

    public Map<String, Resource> getResources()
    {
        return resources;
    }

    public void setResources(Map<String, Resource> resources)
    {
        this.resources = resources;
    }
}
