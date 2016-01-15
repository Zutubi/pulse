package com.zutubi.pulse.master.rest.model;

import java.util.Set;
import java.util.TreeSet;

/**
 * Models a resource version, collecting which agents have the version.
 */
public class ResourceVersionModel
{
    private String versionId;
    private Set<String> agents;

    public ResourceVersionModel(String versionId)
    {
        this.versionId = versionId;
    }

    public String getVersionId()
    {
        return versionId;
    }

    public Set<String> getAgents()
    {
        return agents;
    }

    public void addAgent(String name)
    {
        if (agents == null)
        {
            agents = new TreeSet<>();
        }

        agents.add(name);
    }
}
