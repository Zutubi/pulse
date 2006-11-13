package com.zutubi.pulse.vfs.agent;

import com.zutubi.pulse.SlaveProxyFactory;
import com.zutubi.pulse.model.SlaveManager;
import com.zutubi.pulse.services.ServiceTokenManager;
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
    private SlaveManager slaveManager;
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
        fileSystem.setSlaveManager(slaveManager);
        fileSystem.setServiceTokenManager(serviceTokenManager);
        return fileSystem;
    }

    public Collection getCapabilities()
    {
        return CAPABILITIES;
    }

    public void setSlaveManager(SlaveManager slaveManager)
    {
        this.slaveManager = slaveManager;
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
