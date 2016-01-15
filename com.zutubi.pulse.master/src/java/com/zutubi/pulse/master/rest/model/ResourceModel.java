package com.zutubi.pulse.master.rest.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Models a resource with information about what versions are available on what agents.
 */
public class ResourceModel
{
    private String name;
    private Map<String, ResourceVersionModel> versionsById = new TreeMap<>();

    public ResourceModel(String name)
    {
        this.name = name;
    }

    public String getName()
    {
        return name;
    }

    public List<ResourceVersionModel> getVersions()
    {
        if (versionsById.isEmpty())
        {
            return null;
        }
        else
        {
            return new ArrayList<>(versionsById.values());
        }
    }

    public ResourceVersionModel getOrAddVersion(String versionId)
    {
        ResourceVersionModel version = versionsById.get(versionId);
        if (version == null)
        {
            version = new ResourceVersionModel(versionId);
            versionsById.put(versionId, version);
        }

        return version;
    }
}
