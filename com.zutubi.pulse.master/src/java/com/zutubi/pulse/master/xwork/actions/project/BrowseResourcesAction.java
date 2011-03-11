package com.zutubi.pulse.master.xwork.actions.project;

import com.zutubi.pulse.core.resources.api.ResourceConfiguration;
import com.zutubi.pulse.core.resources.api.ResourceVersionConfiguration;
import com.zutubi.pulse.master.model.ResourceManager;
import com.zutubi.pulse.master.xwork.actions.ActionSupport;

import java.util.*;

/**
 */
public class BrowseResourcesAction extends ActionSupport
{
    private String resourceId;
    private String versionId;
    private String defaultVersionId;

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

    public String getDefaultVersionId()
    {
        return defaultVersionId;
    }

    public void setDefaultVersionId(String defaultVersionId)
    {
        this.defaultVersionId = defaultVersionId;
    }

    public String execute() throws Exception
    {
        Map<String, List<ResourceConfiguration>> resources = resourceManager.findAll();
        for(Map.Entry<String, List<ResourceConfiguration>> entry: resources.entrySet())
        {
            List<ResourceConfiguration> agentResources = entry.getValue();
            for (ResourceConfiguration resource: agentResources)
            {
                Map<String, Set<String>> versions = allResources.get(resource.getName());

                if(versions == null)
                {
                    versions = new TreeMap<String, Set<String>>();
                    allResources.put(resource.getName(), versions);
                }

                mergeVersions(entry.getKey(), resource, versions);
            }
        }

        return SUCCESS;
    }

    private void mergeVersions(String agent, ResourceConfiguration resource, Map<String, Set<String>> versions)
    {
        mergeVersion(agent, "", versions, resource);

        for(ResourceVersionConfiguration version: resource.getVersions().values())
        {
            mergeVersion(agent, version.getValue(), versions, resource);
        }
    }

    private void mergeVersion(String agent, String version, Map<String, Set<String>> versions, ResourceConfiguration resource)
    {
        Set<String> agents = versions.get(version);
        if(agents == null)
        {
            agents = new TreeSet<String>();
            versions.put(version, agents);
        }
        agents.add(agent);
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
