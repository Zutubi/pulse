package com.zutubi.pulse.vfs.pulse;

import com.zutubi.pulse.MasterBuildPaths;
import com.zutubi.pulse.bootstrap.MasterConfigurationManager;
import com.zutubi.pulse.model.BuildManager;
import com.zutubi.pulse.model.BuildResult;
import com.zutubi.util.bean.ObjectFactory;
import org.apache.commons.vfs.FileName;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileSystemOptions;
import org.apache.commons.vfs.provider.AbstractFileSystem;

import java.io.File;
import java.util.Collection;

/**
 * The pulse file system provides access to pulse via a file system style
 * interface.
 */
public class PulseFileSystem extends AbstractFileSystem
{
    private RootFileObject rootFile;
    private ObjectFactory objectFactory;
    private BuildManager buildManager;
    private MasterConfigurationManager configurationManager;

    public PulseFileSystem(final FileName rootName, final FileObject parentLayer, final FileSystemOptions fileSystemOptions)
    {
        super(rootName, parentLayer, fileSystemOptions);
    }

    /**
     * Factory method used by the vfs system to delegate the creation of pulse file objects.
     *
     * @param fileName  the name representing the new file object.
     *
     * @return the new file object instance.
     *
     * @throws Exception
     */
    protected FileObject createFile(final FileName fileName) throws Exception
    {
        // If the file name represents to the root path, then we return the root file object.
        // This is a virtual node that defines the root level data folders.
        String path = fileName.getPath();
        if (path.equals(FileName.ROOT_PATH))
        {
            if(rootFile == null)
            {
                rootFile = objectFactory.buildBean(RootFileObject.class,
                                                   new Class[]{FileName.class, AbstractFileSystem.class},
                                                   new Object[]{fileName, this}
                );
                rootFile.init();
            }

            return rootFile;
        }

        // Delegate the creation of a file object to its parent.
        FileName parentFileName = fileName.getParent();
        AbstractPulseFileObject pfo = (AbstractPulseFileObject) this.resolveFile(parentFileName);
        if (pfo != null)
        {
            AbstractPulseFileObject newFile = pfo.createFile(fileName);
            if (newFile != null)
            {
                newFile.init();
                return newFile;
            }
        }

        // We were unable to resolve the parent file. This means we have no way of knowing what we should be creating.
        // It is best to fail cleanly in this situation.
        throw new FileSystemException(String.format("failed to resolve the requested file: '%s'", fileName.getPath()));
    }

    protected void addCapabilities(Collection caps)
    {
        caps.addAll(PulseFileProvider.CAPABILITIES);
    }

    /**
     * Provide a utility method for retrieving a build recipies base file system path.
     *
     * @param buildId       the build id
     * @param recipeId      the recipe id
     *
     * @return the base.dir for the specified builds recipe.
     */
    protected File getBaseDir(Long buildId, Long recipeId)
    {
        BuildResult result = buildManager.getBuildResult(buildId);
        MasterBuildPaths paths = new MasterBuildPaths(configurationManager);
        return paths.getBaseDir(result, recipeId);
    }

    public void setBuildManager(BuildManager buildManager)
    {
        this.buildManager = buildManager;
    }

    public void setConfigurationManager(MasterConfigurationManager configurationManager)
    {
        this.configurationManager = configurationManager;
    }

    public MasterConfigurationManager getConfigurationManager()
    {
        return configurationManager;
    }

    public void setObjectFactory(ObjectFactory objectFactory)
    {
        this.objectFactory = objectFactory;
    }
}
