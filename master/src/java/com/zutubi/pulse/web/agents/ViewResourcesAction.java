/********************************************************************************
 @COPYRIGHT@
 ********************************************************************************/
package com.zutubi.pulse.web.agents;

import com.zutubi.pulse.ResourceNameComparator;
import com.zutubi.pulse.web.ActionSupport;
import com.zutubi.pulse.core.model.Resource;
import com.zutubi.pulse.model.persistence.ResourceDao;
import com.zutubi.pulse.model.PersistentResource;
import com.zutubi.pulse.model.ResourceManager;
import com.zutubi.pulse.model.SlaveManager;

import java.util.Collections;
import java.util.List;

/**
 */
public class ViewResourcesAction extends AgentActionSupport
{
    private ResourceManager resourceManager;
    private List<PersistentResource> resources;

    public List<PersistentResource> getResources()
    {
        return resources;
    }

    public String execute() throws Exception
    {
        lookupSlave();
        resources = resourceManager.findBySlave(slave);
        Collections.sort(resources, new ResourceNameComparator());
        return SUCCESS;
    }

    public void setResourceManager(ResourceManager resourceManager)
    {
        this.resourceManager = resourceManager;
    }
}
