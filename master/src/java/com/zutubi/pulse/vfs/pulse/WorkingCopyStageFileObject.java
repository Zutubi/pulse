package com.zutubi.pulse.vfs.pulse;

import com.zutubi.pulse.MasterBuildPaths;
import com.zutubi.pulse.bootstrap.MasterConfigurationManager;
import com.zutubi.pulse.core.model.RecipeResult;
import com.zutubi.pulse.model.BuildResult;
import com.zutubi.pulse.model.RecipeResultNode;
import org.apache.commons.vfs.FileName;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileType;
import org.apache.commons.vfs.provider.AbstractFileSystem;

import java.io.File;

/**
 * <class comment/>
 */
public class WorkingCopyStageFileObject extends AbstractPulseFileObject implements RecipeResultProvider
{
    private MasterConfigurationManager configurationManager;

    private final String STAGE_FORMAT = "stage :: %s :: %s@%s";

    private final long recipeId;

    private String displayName;

    public WorkingCopyStageFileObject(final FileName name, final long recipeId, final AbstractFileSystem fs)
    {
        super(name, fs);

        this.recipeId = recipeId;
    }

    public AbstractPulseFileObject createFile(final FileName fileName) throws Exception
    {
        File newBase = new File(getWorkingCopyBase(), fileName.getBaseName());

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

        return getWorkingCopyBase().list();
    }

    protected File getWorkingCopyBase() throws FileSystemException
    {
        BuildResultProvider provider = (BuildResultProvider) getAncestor(BuildResultProvider.class);
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
        if (displayName == null)
        {
            RecipeResultNode node = buildManager.getResultNodeByResultId(recipeId);
            displayName = String.format(STAGE_FORMAT, node.getStage(), node.getResult().getRecipeNameSafe(), node.getHostSafe());
        }
        return this.displayName;
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
