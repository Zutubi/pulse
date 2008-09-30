package com.zutubi.pulse.slave;

import com.zutubi.pulse.core.ResourceRepository;
import com.zutubi.pulse.core.BuildException;
import com.zutubi.pulse.core.config.Resource;
import com.zutubi.pulse.services.MasterService;
import com.zutubi.pulse.services.ServiceTokenManager;
import com.zutubi.pulse.model.ResourceRequirement;
import com.zutubi.util.logging.Logger;

import java.util.List;

/**
 */
public class RemoteResourceRepository implements ResourceRepository
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

    public boolean hasResource(ResourceRequirement requirement)
    {
        String name = requirement.getResource();
        String version = requirement.getVersion();
        
        Resource r = getResource(name);
        if (r == null)
        {
            return false;
        }

        return requirement.isDefaultVersion() || r.getVersion(version) != null;
    }

    public boolean hasResource(String name)
    {
        return getResource(name) != null;
    }

    public Resource getResource(String name)
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

    public List<String> getResourceNames()
    {
        return masterProxy.getResourceNames(serviceTokenManager.getToken(), handle);
    }
}
