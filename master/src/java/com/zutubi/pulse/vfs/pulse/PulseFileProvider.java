package com.zutubi.pulse.vfs.pulse;

import org.apache.commons.vfs.*;
import org.apache.commons.vfs.provider.AbstractOriginatingFileProvider;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import com.zutubi.pulse.model.BuildManager;
import com.zutubi.pulse.model.ProjectManager;
import com.zutubi.pulse.search.Queries;
import com.zutubi.pulse.bootstrap.MasterConfigurationManager;

/**
 * <class comment/>
 */
public class PulseFileProvider extends AbstractOriginatingFileProvider
{
    private BuildManager buildManager;
    private ProjectManager projectManager;
    private MasterConfigurationManager configurationManager;
    private Queries queries;

    final static Collection CAPABILITIES = Collections.unmodifiableCollection(Arrays.asList(
        Capability.GET_TYPE,
        Capability.READ_CONTENT,
        Capability.URI,
        Capability.GET_LAST_MODIFIED
    ));

    public PulseFileProvider()
    {
        setFileNameParser(PulseFileNameParser.getInstance());
    }

    protected FileSystem doCreateFileSystem(final FileName rootName, final FileSystemOptions fileSystemOptions) throws FileSystemException
    {
        PulseFileSystem fileSystem = new PulseFileSystem(rootName, null, fileSystemOptions);
        fileSystem.setBuildManager(buildManager);
        fileSystem.setProjectManager(projectManager);
        fileSystem.setQueries(queries);
        fileSystem.setConfigurationManager(configurationManager);
        return fileSystem;
    }

    public Collection getCapabilities()
    {
        return CAPABILITIES;
    }

    public void setBuildManager(BuildManager buildManager)
    {
        this.buildManager = buildManager;
    }

    public void setProjectManager(ProjectManager projectManager)
    {
        this.projectManager = projectManager;
    }

    public void setQueries(Queries queries)
    {
        this.queries = queries;
    }

    public void setConfigurationManager(MasterConfigurationManager configurationManager)
    {
        this.configurationManager = configurationManager;
    }
}