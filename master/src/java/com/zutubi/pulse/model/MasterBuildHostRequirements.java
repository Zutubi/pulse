/********************************************************************************
 @COPYRIGHT@
 ********************************************************************************/
package com.zutubi.pulse.model;

import com.zutubi.pulse.BuildService;
import com.zutubi.pulse.MasterBuildService;

/**
 */
public class MasterBuildHostRequirements extends AbstractBuildHostRequirements
{

    public boolean fulfilledBy(BuildService service)
    {
        return service instanceof MasterBuildService;
    }

    public String getSummary()
    {
        return "[master]";
    }
}
