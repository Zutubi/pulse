package com.zutubi.pulse.model;

import com.zutubi.pulse.BuildService;
import com.zutubi.pulse.SlaveBuildService;
import com.zutubi.pulse.SlaveProxyFactory;
import com.zutubi.pulse.core.PulseRuntimeException;

import java.net.MalformedURLException;

/**
 * Provides a SlaveService for handling builds.
 */
public class SlaveBuildServiceResolver extends AbstractBuildServiceResolver
{
    private Slave slave;
    private SlaveProxyFactory factory;

    public SlaveBuildServiceResolver()
    {

    }

    public SlaveBuildServiceResolver(Slave slave)
    {
        this.slave = slave;
    }

    public BuildService resolve()
    {
        try
        {
            return new SlaveBuildService(slave, factory.createProxy(slave));
        }
        catch (MalformedURLException e)
        {
            throw new PulseRuntimeException("Error contacting slave '" + slave.getName() + "': " + e.getMessage(), e);
        }
    }

    public String getHostName()
    {
        return slave.getName();
    }

    private Slave getSlave()
    {
        return slave;
    }

    private void setSlave(Slave slave)
    {
        this.slave = slave;
    }

    public void setFactory(SlaveProxyFactory factory)
    {
        this.factory = factory;
    }
}
