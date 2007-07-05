package com.zutubi.pulse.model;

import com.opensymphony.util.TextUtils;
import com.zutubi.prototype.config.ConfigurationProvider;
import com.zutubi.prototype.type.record.PathUtils;
import com.zutubi.pulse.core.ConfigurableResourceRepository;
import com.zutubi.pulse.core.FileLoadException;
import com.zutubi.pulse.core.config.Resource;
import com.zutubi.pulse.core.config.ResourceVersion;
import com.zutubi.pulse.prototype.config.agent.AgentConfiguration;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * A resource repository backed by the configuration subsystem.
 */
public class ConfigurationResourceRepository implements ConfigurableResourceRepository
{
    private AgentConfiguration agentConfig;
    private ConfigurationProvider configurationProvider;

    public ConfigurationResourceRepository(AgentConfiguration agentConfig, ConfigurationProvider configurationProvider)
    {
        this.agentConfig = agentConfig;
        this.configurationProvider = configurationProvider;
    }

    public boolean hasResource(String name, String version)
    {
        Resource r = getResource(name);
        return r != null && (!TextUtils.stringSet(version) || r.hasVersion(version));
    }

    public boolean hasResource(String name)
    {
        return getResource(name) != null;
    }

    public Resource getResource(String name)
    {
        return agentConfig.getResources().get(name);
    }

    public List<String> getResourceNames()
    {
        return new LinkedList<String>(agentConfig.getResources().keySet());
    }

    public void addResource(Resource resource)
    {
        addResource(resource, false);
    }

    public void addResource(Resource resource, boolean overwrite)
    {
        // merge this new resource with existing resources.  The overwrite refers to properties that already exist.
        Resource existingResource = getResource(resource.getName());
        if (existingResource == null)
        {
            configurationProvider.insert(getResourcesPath(), resource);
        }
        else
        {
            // FIXME: do we have a better way to programmatically update an
            // FIXME: existing instance?
            // Remove the existing instance before we play with it.
            configurationProvider.delete(resource.getConfigurationPath());

            // we have an existing resource, so merge the details.
            for (String propertyName: resource.getProperties().keySet())
            {
                if (existingResource.hasProperty(propertyName))
                {
                    if (overwrite)
                    {
                        existingResource.deleteProperty(propertyName);
                        existingResource.addProperty(resource.getProperty(propertyName));
                    }
                }
                else
                {
                    existingResource.addProperty(resource.getProperty(propertyName));
                }
            }

            for (String versionStr : resource.getVersions().keySet())
            {
                if (!existingResource.hasVersion(versionStr))
                {
                    existingResource.add(resource.getVersion(versionStr));
                }
                else
                {
                    ResourceVersion version = resource.getVersion(versionStr);
                    ResourceVersion existingVersion = existingResource.getVersion(versionStr);

                    for (String propertyName: version.getProperties().keySet())
                    {
                        try
                        {
                            if (existingVersion.hasProperty(propertyName))
                            {
                                if (overwrite)
                                {
                                    existingVersion.deleteProperty(propertyName);
                                    existingVersion.addProperty(version.getProperty(propertyName));
                                }
                            }
                            else
                            {
                                existingVersion.addProperty(version.getProperty(propertyName));
                            }
                        }
                        catch (FileLoadException e)
                        {
                            // should never happen.
                            e.printStackTrace();
                        }
                    }
                }
            }

            configurationProvider.insert(getResourcesPath(), existingResource);
        }
    }

    public Map<String, Resource> getAll()
    {
        return agentConfig.getResources();
    }

    private String getResourcesPath()
    {
        return PathUtils.getPath(agentConfig.getConfigurationPath(), "resources");
    }
}
