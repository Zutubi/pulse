/********************************************************************************
 @COPYRIGHT@
 ********************************************************************************/
package com.zutubi.pulse.model;

import com.zutubi.pulse.BuildService;
import com.zutubi.pulse.SlaveBuildService;

/**
 */
public class SlaveBuildHostRequirements extends AbstractBuildHostRequirements
{
    private Slave slave;

    public SlaveBuildHostRequirements()
    {

    }

    public SlaveBuildHostRequirements(Slave slave)
    {
        this.slave = slave;
    }

    public SlaveBuildHostRequirements copy()
    {
        // Don't deep copy the slave reference!
        return new SlaveBuildHostRequirements(slave);
    }

    public boolean fulfilledBy(BuildService service)
    {
        if (service instanceof SlaveBuildService)
        {
            SlaveBuildService slaveService = (SlaveBuildService) service;
            return slaveService.getSlave().getId() == slave.getId();
        }

        return false;
    }

    public String getSummary()
    {
        return slave.getName();
    }

    public Slave getSlave()
    {
        return slave;
    }

    private void setSlave(Slave slave)
    {
        this.slave = slave;
    }
}
