package com.zutubi.pulse.master.tove.config.admin;

import com.zutubi.tove.annotations.SymbolicName;
import com.zutubi.tove.annotations.Reference;
import com.zutubi.tove.config.api.AbstractConfiguration;
import com.zutubi.pulse.master.tove.config.group.GroupConfiguration;

import java.util.List;
import java.util.LinkedList;

/**
 * The configuration that controls the embedded artifact repository.
 */
@SymbolicName("zutubi.repositoryConfig")
public class RepositoryConfiguration extends AbstractConfiguration
{
    /**
     * The list of groups that by default have read access to the repository.
     */
    @Reference
    private List<GroupConfiguration> readAccess = new LinkedList<GroupConfiguration>();

    /**
     * The list of groups that by default have write access to the repository.
     */
    @Reference
    private List<GroupConfiguration> writeAccess = new LinkedList<GroupConfiguration>();

    public List<GroupConfiguration> getReadAccess()
    {
        return readAccess;
    }

    public void setReadAccess(List<GroupConfiguration> readAccess)
    {
        this.readAccess = readAccess;
    }

    public List<GroupConfiguration> getWriteAccess()
    {
        return writeAccess;
    }

    public void setWriteAccess(List<GroupConfiguration> writeAccess)
    {
        this.writeAccess = writeAccess;
    }
}
