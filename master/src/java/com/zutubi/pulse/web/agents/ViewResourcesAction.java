package com.zutubi.pulse.web.agents;

import com.zutubi.pulse.model.NamedEntityComparator;
import com.zutubi.pulse.model.PersistentResource;
import com.zutubi.pulse.model.ResourceManager;

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
        Collections.sort(resources, new NamedEntityComparator());
        return SUCCESS;
    }

    public void setResourceManager(ResourceManager resourceManager)
    {
        this.resourceManager = resourceManager;
    }
}
