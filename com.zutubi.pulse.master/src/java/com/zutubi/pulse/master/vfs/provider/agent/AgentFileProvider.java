package com.zutubi.pulse.master.vfs.provider.agent;

import com.zutubi.pulse.master.agent.AgentManager;
import com.zutubi.pulse.master.agent.SlaveProxyFactory;
import com.zutubi.pulse.servercore.services.ServiceTokenManager;
import org.apache.commons.vfs.*;
import org.apache.commons.vfs.provider.AbstractOriginatingFileProvider;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

/**
 * <class comment/>
 */
public class AgentFileProvider extends AbstractOriginatingFileProvider
{
    private AgentManager agentManager;
    private SlaveProxyFactory proxyFactory;
    private ServiceTokenManager serviceTokenManager;

    final static Collection CAPABILITIES = Collections.unmodifiableCollection(Arrays.asList(
        Capability.GET_TYPE,
        Capability.READ_CONTENT,
        Capability.URI,
        Capability.GET_LAST_MODIFIED
    ));


    public AgentFileProvider()
    {
        setFileNameParser(new AgentFileNameParser());
    }

    protected FileSystem doCreateFileSystem(final FileName rootName, final FileSystemOptions options) throws FileSystemException
    {
        AgentFileSystem fileSystem = new AgentFileSystem(rootName, null, options);
        fileSystem.setProxyFactory(proxyFactory);
        fileSystem.setAgentManager(agentManager);
        fileSystem.setServiceTokenManager(serviceTokenManager);
        return fileSystem;
    }

    public Collection getCapabilities()
    {
        return CAPABILITIES;
    }

    public void setAgentManager(AgentManager agentManager)
    {
        this.agentManager = agentManager;
    }

    public void setSlaveProxyFactory(SlaveProxyFactory proxyFactory)
    {
        this.proxyFactory = proxyFactory;
    }

    public void setServiceTokenManager(ServiceTokenManager serviceTokenManager)
    {
        this.serviceTokenManager = serviceTokenManager;
    }
}
