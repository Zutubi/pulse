package com.zutubi.pulse.slave;

import com.zutubi.pulse.core.ResourceRepository;
import com.zutubi.pulse.core.BuildException;
import com.zutubi.pulse.core.model.Resource;
import com.zutubi.pulse.services.MasterService;
import com.zutubi.pulse.util.logging.Logger;
import com.caucho.hessian.client.HessianRuntimeException;

import java.util.List;

/**
 */
public class RemoteResourceRepository implements ResourceRepository
{
    private static final Logger LOG = Logger.getLogger(RemoteResourceRepository.class);

    private long slaveId;
    private MasterService masterProxy;

    public RemoteResourceRepository(long slaveId, MasterService masterProxy)
    {
        this.slaveId = slaveId;
        this.masterProxy = masterProxy;
    }

    public boolean hasResource(String name, String version)
    {
        Resource r = getResource(name);
        return r != null && r.getVersion(version) != null;
    }

    public boolean hasResource(String name)
    {
        return getResource(name) != null;
    }

    public Resource getResource(String name)
    {
        try
        {
            return masterProxy.getResource(slaveId, name);
        }
        catch (HessianRuntimeException e)
        {
            LOG.severe(e);
            throw new BuildException("Unable to retrieve details of resource '" + name + "' from master: " + e.getMessage());
        }
    }

    public List<String> getResourceNames()
    {
        return masterProxy.getResourceNames(slaveId);
    }
}
