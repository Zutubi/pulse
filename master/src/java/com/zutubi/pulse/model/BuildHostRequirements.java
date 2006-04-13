/********************************************************************************
 @COPYRIGHT@
 ********************************************************************************/
package com.zutubi.pulse.model;

import com.zutubi.pulse.BuildService;

/**
 */
public interface BuildHostRequirements
{
    public boolean fulfilledBy(BuildService service);

    public String getSummary();
}
