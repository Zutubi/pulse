package com.cinnamonbob.model;

import com.cinnamonbob.BuildService;
import com.cinnamonbob.SlaveBuildService;
import com.cinnamonbob.SlaveProxyFactory;
import com.cinnamonbob.core.BobRuntimeException;

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
            throw new BobRuntimeException("Error contacting slave '" + slave.getName() + "': " + e.getMessage(), e);
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
