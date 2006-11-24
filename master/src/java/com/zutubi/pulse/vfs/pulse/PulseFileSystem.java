package com.zutubi.pulse.vfs.pulse;

import org.apache.commons.vfs.FileName;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemOptions;
import org.apache.commons.vfs.provider.AbstractFileSystem;

import java.util.Collection;
import java.io.File;

import com.zutubi.pulse.model.BuildManager;
import com.zutubi.pulse.model.ProjectManager;
import com.zutubi.pulse.model.BuildResult;
import com.zutubi.pulse.search.Queries;
import com.zutubi.pulse.bootstrap.MasterConfigurationManager;
import com.zutubi.pulse.MasterBuildPaths;

/**
 * <class comment/>
 */
public class PulseFileSystem extends AbstractFileSystem
{
    private BuildManager buildManager;
    private ProjectManager projectManager;
    private Queries queries;
    private MasterConfigurationManager configurationManager;

    public PulseFileSystem(final FileName rootName, final FileObject parentLayer, final FileSystemOptions fileSystemOptions)
    {
        super(rootName, parentLayer, fileSystemOptions);
    }

    protected FileObject createFile(final FileName fileName) throws Exception
    {
        // create the appropriate file object based on the file name.
        String path = fileName.getPath();
        if (path.equals(FileName.ROOT_PATH))
        {
            return new RootFileObject(fileName, this);
        }

        // as the parent to create this node since it holds all of the contextual information.
        PulseFileObject pfo = (PulseFileObject) this.resolveFile(fileName.getParent());
        return pfo.createFile(fileName);
    }

    protected void addCapabilities(Collection caps)
    {
        caps.addAll(PulseFileProvider.CAPABILITIES);
    }

    public File getBaseDir(Long buildId, Long recipeId)
    {
        BuildResult result = buildManager.getBuildResult(buildId);
        MasterBuildPaths paths = new MasterBuildPaths(configurationManager);
        return paths.getBaseDir(result, recipeId);
    }

    public File getArtifactDir()
    {
        MasterBuildPaths paths = new MasterBuildPaths(configurationManager);
//        paths.getOutputDir()
        return null;
    }

    public void setBuildManager(BuildManager buildManager)
    {
        this.buildManager = buildManager;
    }

    public BuildManager getBuildManager()
    {
        return buildManager;
    }

    public ProjectManager getProjectManager()
    {
        return projectManager;
    }

    public void setProjectManager(ProjectManager projectManager)
    {
        this.projectManager = projectManager;
    }

    public void setQueries(Queries queries)
    {
        this.queries = queries;
    }

    public Queries getQueries()
    {
        return queries;
    }

    public MasterConfigurationManager getConfigurationManager()
    {
        return configurationManager;
    }

    public void setConfigurationManager(MasterConfigurationManager configurationManager)
    {
        this.configurationManager = configurationManager;
    }
}
