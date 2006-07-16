package com.zutubi.pulse.web.project;

import com.zutubi.pulse.core.model.ResourceVersion;
import com.zutubi.pulse.model.PersistentResource;
import com.zutubi.pulse.model.ResourceManager;
import com.zutubi.pulse.model.Slave;
import com.zutubi.pulse.web.ActionSupport;

import java.util.*;

/**
 */
public class BrowseResourcesAction extends ActionSupport
{
    private String resourceId;
    private String versionId;
    private ResourceManager resourceManager;
    /**
     * Maps from resource name to a map from version value to agents that
     * have that version.
     */
    private Map<String, Map<String, Set<String>>> allResources = new TreeMap<String, Map<String, Set<String>>>();

    public String getResourceId()
    {
        return resourceId;
    }

    public void setResourceId(String resourceId)
    {
        this.resourceId = resourceId;
    }

    public String getVersionId()
    {
        return versionId;
    }

    public void setVersionId(String versionId)
    {
        this.versionId = versionId;
    }

    public Map<String,Map<String,Set<String>>> getAllResources()
    {
        return allResources;
    }

    public String execute() throws Exception
    {
        List<PersistentResource> resources = resourceManager.findAll();
        for(PersistentResource resource: resources)
        {
            Map<String, Set<String>> versions = allResources.get(resource.getName());

            if(versions == null)
            {
                versions = new TreeMap<String, Set<String>>();
                allResources.put(resource.getName(), versions);
            }

            mergeVersions(resource, versions);
        }

        return SUCCESS;
    }

    private void mergeVersions(PersistentResource resource, Map<String,Set<String>> versions)
    {
        mergeVersion("", versions, resource);

        for(ResourceVersion version: resource.getVersions().values())
        {
            mergeVersion(version.getValue(), versions, resource);
        }
    }

    private void mergeVersion(String version, Map<String, Set<String>> versions, PersistentResource resource)
    {
        Set<String> agents = versions.get(version);
        if(agents == null)
        {
            agents = new TreeSet<String>();
            versions.put(version, agents);
        }
        agents.add(getAgentName(resource));
    }

    private String getAgentName(PersistentResource resource)
    {
        Slave slave = resource.getSlave();
        if(slave == null)
        {
            return "master";
        }
        else
        {
            return slave.getName();
        }
    }

    public void setResourceManager(ResourceManager resourceManager)
    {
        this.resourceManager = resourceManager;
    }

    public class VersionInfo
    {
        public String value;
        public Set<String> agents;

        public VersionInfo(String value)
        {
            this.value = value;
            agents = new TreeSet<String>();
        }

        public boolean equals(Object obj)
        {
            return (obj instanceof VersionInfo) && value.equals(((VersionInfo)obj).value);
        }

        public int hashCode()
        {
            return value.hashCode();
        }
    }
}
