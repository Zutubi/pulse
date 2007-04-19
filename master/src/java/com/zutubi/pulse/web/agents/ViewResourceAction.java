package com.zutubi.pulse.web.agents;

import com.zutubi.pulse.core.model.Resource;
import com.zutubi.pulse.core.model.ResourceVersion;
import com.zutubi.pulse.model.PersistentResource;
import com.zutubi.pulse.model.ResourceManager;
import com.zutubi.util.Sort;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

/**
 * 
 *
 */
public class ViewResourceAction extends AgentActionSupport
{
    private long resourceId;
    private PersistentResource resource;
    private ResourceManager resourceManager;
    private long selectedNode;
    private List<ResourceVersion> versions;

    public long getResourceId()
    {
        return resourceId;
    }

    public void setResourceId(long resourceId)
    {
        this.resourceId = resourceId;
    }

    public Resource getResource()
    {
        return resource;
    }

    public void setResource(PersistentResource resource)
    {
        this.resource = resource;
    }

    public List<ResourceVersion> getVersions()
    {
        return versions;
    }

    public long getSelectedNode()
    {
        return selectedNode;
    }

    public void setSelectedNode(long selectedNode)
    {
        this.selectedNode = selectedNode;
    }

    public boolean haveSelectedNode()
    {
        return selectedNode != 0L;
    }

    public boolean isDefaultVersion(String version)
    {
        return version.equals(resource.getDefaultVersion());
    }

    public String execute()
    {
        resource = resourceManager.findById(resourceId);
        if (resource == null)
        {
            addActionError("Unknown resource [" + resourceId + "]");
            return ERROR;
        }

        slave = resource.getSlave();
        versions = new LinkedList<ResourceVersion>(resource.getVersions().values());

        final Comparator<String> comp = new Sort.StringComparator();
        Collections.sort(versions, new Comparator<ResourceVersion>()
        {
            public int compare(ResourceVersion v1, ResourceVersion v2)
            {
                return comp.compare(v1.getValue(), v2.getValue());
            }
        });
        
        return SUCCESS;
    }

    public void setResourceManager(ResourceManager resourceManager)
    {
        this.resourceManager = resourceManager;
    }
}
