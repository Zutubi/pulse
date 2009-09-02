package com.zutubi.pulse.slave;

import com.zutubi.pulse.core.ResourceRepositorySupport;
import com.zutubi.pulse.core.config.ResourceConfiguration;
import com.zutubi.pulse.core.engine.api.BuildException;
import com.zutubi.pulse.servercore.services.MasterService;
import com.zutubi.pulse.servercore.services.ServiceTokenManager;
import com.zutubi.util.logging.Logger;

/**
 * A resource repository that lives remotely on the master server.
 */
public class RemoteResourceRepository extends ResourceRepositorySupport
{
    private static final Logger LOG = Logger.getLogger(RemoteResourceRepository.class);

    private long handle;
    private MasterService masterProxy;
    private ServiceTokenManager serviceTokenManager;

    public RemoteResourceRepository(long handle, MasterService masterProxy, ServiceTokenManager serviceTokenManager)
    {
        this.handle = handle;
        this.masterProxy = masterProxy;
        this.serviceTokenManager = serviceTokenManager;
    }

    public ResourceConfiguration getResource(String name)
    {
        try
        {
            return masterProxy.getResource(serviceTokenManager.getToken(), handle, name);
        }
        catch (RuntimeException e)
        {
            LOG.severe(e);
            throw new BuildException("Unable to retrieve details of resource '" + name + "' from master: " + e.getMessage());
        }
    }
}
