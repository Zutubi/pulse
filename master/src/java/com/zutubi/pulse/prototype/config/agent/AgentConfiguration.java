package com.zutubi.pulse.prototype.config.agent;

import com.zutubi.config.annotations.ControllingCheckbox;
import com.zutubi.config.annotations.Form;
import com.zutubi.config.annotations.Internal;
import com.zutubi.config.annotations.NoInherit;
import com.zutubi.config.annotations.SymbolicName;
import com.zutubi.config.annotations.Table;
import com.zutubi.pulse.core.config.AbstractConfiguration;
import com.zutubi.pulse.core.config.NamedConfiguration;
import com.zutubi.pulse.core.config.Resource;
import com.zutubi.validation.annotations.Numeric;
import com.zutubi.validation.annotations.Required;

import java.util.Map;

/**
 */
@Form(fieldOrder = {"name", "remote", "host", "port"})
@Table(columns = {"name", "location", "status"})
@SymbolicName("zutubi.agentConfig")
public class AgentConfiguration extends AbstractConfiguration implements NamedConfiguration
{
    @Internal
    private long agentStateId;
    @ControllingCheckbox(dependentFields = {"host", "port"})
    private boolean remote = true;
    @NoInherit
    private String name;
    @Required
    private String host;
    @Numeric(min = 1)
    private int port = 8090;
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

    public long getAgentStateId()
    {
        return agentStateId;
    }

    public void setAgentStateId(long agentStateId)
    {
        this.agentStateId = agentStateId;
    }

    public Map<String, Resource> getResources()
    {
        return resources;
    }

    public void setResources(Map<String, Resource> resources)
    {
        this.resources = resources;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }
}
