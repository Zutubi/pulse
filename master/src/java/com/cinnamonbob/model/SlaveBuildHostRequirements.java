package com.cinnamonbob.model;

import com.cinnamonbob.BuildService;
import com.cinnamonbob.SlaveBuildService;

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

    public boolean fulfilledBy(BuildService service)
    {
        if (service instanceof SlaveBuildService)
        {
            SlaveBuildService slaveService = (SlaveBuildService) service;
            return slaveService.getSlave().getId() == slave.getId();
        }

        return false;
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
