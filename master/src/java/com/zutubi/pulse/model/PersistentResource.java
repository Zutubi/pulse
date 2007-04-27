package com.zutubi.pulse.model;

import com.zutubi.pulse.core.FileLoadException;
import com.zutubi.pulse.core.config.ResourceProperty;
import com.zutubi.pulse.core.config.Resource;
import com.zutubi.pulse.core.config.ResourceVersion;

/**
 */
public class PersistentResource extends Resource
{
    /**
     * The slave that owns this resource, or null if it is owned by the master.
     */
    private AgentState agentState;

    public PersistentResource()
    {
    }

    public PersistentResource(String name)
    {
        super(name);
    }

    public PersistentResource(String name, AgentState agentState)
    {
        super(name);
        this.agentState = agentState;
    }

    public PersistentResource(Resource resource, AgentState agentState)
    {
        // Shallow copy: we take ownership
        this.setName(resource.getName());
        this.setProperties(resource.getProperties());
        this.setDefaultVersion(resource.getDefaultVersion());
        this.setVersions(resource.getVersions());

        this.agentState = agentState;
    }

    public Resource asResource()
    {
        // Deep copy the other way: don't want hibernate proxies in result.
        Resource resource = new Resource(getName());
        for(ResourceProperty p: getProperties().values())
        {
            resource.addProperty(new ResourceProperty(p.getName(), p.getValue(), p.getAddToEnvironment(), p.getAddToPath(), p.getResolveVariables()));
        }

        for(ResourceVersion v: getVersions().values())
        {
            ResourceVersion copy = new ResourceVersion(v.getValue());
            for(ResourceProperty p: v.getProperties().values())
            {
                try
                {
                    copy.addProperty(new ResourceProperty(p.getName(), p.getValue(), p.getAddToEnvironment(), p.getAddToPath(), p.getResolveVariables()));
                }
                catch (FileLoadException e)
                {
                    // Impossible
                }
            }

            resource.add(copy);
        }

        return resource;
    }

    public AgentState getSlave()
    {
        return agentState;
    }

    public void setSlave(AgentState agentState)
    {
        this.agentState = agentState;
    }

}
