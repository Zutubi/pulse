package com.zutubi.pulse.master.vfs.provider.pulse;

import com.zutubi.pulse.core.model.RecipeResult;
import com.zutubi.pulse.master.MasterBuildPaths;
import com.zutubi.pulse.master.bootstrap.MasterConfigurationManager;
import com.zutubi.pulse.master.model.BuildResult;
import com.zutubi.pulse.master.model.RecipeResultNode;
import org.apache.commons.vfs.FileName;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileType;
import org.apache.commons.vfs.provider.AbstractFileSystem;
import org.apache.commons.vfs.provider.UriParser;

import java.io.File;

/**
 * <class comment/>
 */
public class WorkingCopyStageFileObject extends AbstractPulseFileObject implements RecipeResultProvider
{
    private MasterConfigurationManager configurationManager;

    private final String STAGE_FORMAT = "stage :: %s :: %s@%s";

    private final long recipeId;

    private static final String NO_WORKING_COPY_AVAILABLE = "no working copy available";

    public WorkingCopyStageFileObject(final FileName name, final long recipeId, final AbstractFileSystem fs)
    {
        super(name, fs);

        this.recipeId = recipeId;
    }

    public AbstractPulseFileObject createFile(final FileName fileName) throws FileSystemException
    {
        String child = fileName.getBaseName();
        if (child.equals(NO_WORKING_COPY_AVAILABLE))
        {
            return objectFactory.buildBean(TextMessageFileObject.class,
                    new Class[]{FileName.class, String.class, AbstractFileSystem.class},
                    new Object[]{fileName, FileTypeConstants.MESSAGE, pfs}
            );
        }

        File newBase = new File(getWorkingCopyBase(), child);

        return objectFactory.buildBean(WorkingCopyFileObject.class,
                new Class[]{FileName.class, File.class, AbstractFileSystem.class},
                new Object[]{fileName, newBase, pfs}
        );
    }

    protected FileType doGetType() throws Exception
    {
        return FileType.FOLDER;
    }

    protected String[] doListChildren() throws Exception
    {
        if (!getRecipeResult().completed())
        {
            // only look at the commands is the recipe is complete.
            return new String[0];
        }

        File copyBase = getWorkingCopyBase();
        if (copyBase.isDirectory())
        {
            return UriParser.encode(copyBase.list());
        }
        
        return new String[]{NO_WORKING_COPY_AVAILABLE};
    }

    protected File getWorkingCopyBase() throws FileSystemException
    {
        BuildResultProvider provider = getAncestor(BuildResultProvider.class);
        if (provider == null)
        {
            return null;
        }

        BuildResult buildResult = provider.getBuildResult();
        MasterBuildPaths paths = new MasterBuildPaths(configurationManager);
        return paths.getBaseDir(buildResult, recipeId);
    }

    public String getDisplayName()
    {
        RecipeResultNode node = buildManager.getResultNodeByResultId(recipeId);
        return String.format(STAGE_FORMAT, node.getStageName(), node.getResult().getRecipeNameSafe(), node.getHostSafe());
    }

    public RecipeResult getRecipeResult()
    {
        return buildManager.getRecipeResult(getRecipeResultId());
    }

    public long getRecipeResultId()
    {
        return recipeId;
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
}
