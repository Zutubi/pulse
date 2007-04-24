package com.zutubi.pulse.prototype.config.agent;

import com.zutubi.config.annotations.SymbolicName;
import com.zutubi.validation.annotations.Numeric;
import com.zutubi.pulse.core.config.Resource;
import com.zutubi.pulse.core.config.AbstractConfiguration;

import java.util.Map;

/**
 */
@SymbolicName("internal.agentConfig")
public class AgentConfiguration extends AbstractConfiguration
{
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
}
