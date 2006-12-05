package com.zutubi.pulse.vfs.pulse;

import com.zutubi.pulse.MasterBuildPaths;
import com.zutubi.pulse.bootstrap.MasterConfigurationManager;
import com.zutubi.pulse.core.ObjectFactory;
import com.zutubi.pulse.model.BuildManager;
import com.zutubi.pulse.model.BuildResult;
import org.apache.commons.vfs.FileName;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileSystemOptions;
import org.apache.commons.vfs.provider.AbstractFileSystem;

import java.io.File;
import java.util.Collection;

/**
 * The pulse file system provides access to the pulse via a file system style interface.
 *
 * 
 */
public class PulseFileSystem extends AbstractFileSystem
{
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
            AbstractPulseFileObject newFile = objectFactory.buildBean(RootFileObject.class,
                    new Class[]{FileName.class, AbstractFileSystem.class},
                    new Object[]{fileName, this}
            );
            newFile.init();
            return newFile;
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

    // The pulse file system provides the various file object implementations with access to the

    /**
     * Required resource.
     *
     * @param buildManager instance.
     */
    public void setBuildManager(BuildManager buildManager)
    {
        this.buildManager = buildManager;
    }

    /**
     * Required resource.
     *
     * @param configurationManager instance.
     */
    public void setConfigurationManager(MasterConfigurationManager configurationManager)
    {
        this.configurationManager = configurationManager;
    }

    public MasterConfigurationManager getConfigurationManager()
    {
        return configurationManager;
    }

    /**
     * Required resource.
     * 
     * @param objectFactory instance
     */
    public void setObjectFactory(ObjectFactory objectFactory)
    {
        this.objectFactory = objectFactory;
    }
}
