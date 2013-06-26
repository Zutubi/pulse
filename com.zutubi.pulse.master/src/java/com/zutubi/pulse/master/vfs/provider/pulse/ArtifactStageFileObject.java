package com.zutubi.pulse.master.vfs.provider.pulse;

import com.zutubi.pulse.core.model.CommandResult;
import com.zutubi.pulse.core.model.RecipeResult;
import com.zutubi.pulse.master.model.RecipeResultNode;
import org.apache.commons.vfs.FileName;
import org.apache.commons.vfs.FileType;
import org.apache.commons.vfs.provider.AbstractFileSystem;

import java.util.LinkedList;
import java.util.List;

/**
 * File object representing a stage in the artifacts tree.
 */
public class ArtifactStageFileObject extends AbstractPulseFileObject implements RecipeResultProvider
{
    private static final String STAGE_FORMAT = "stage :: %s :: %s@%s";

    private final long recipeId;

    private static final String IN_PROGRESS = "this stage is currently in progress";

    public ArtifactStageFileObject(final FileName name, final long recipeId, final AbstractFileSystem fs)
    {
        super(name, fs);
        
        this.recipeId = recipeId;
    }

    public AbstractPulseFileObject createFile(final FileName fileName)
    {
        String childName = fileName.getBaseName();
        if (childName.equals(IN_PROGRESS))
        {
            return objectFactory.buildBean(TextMessageFileObject.class, fileName, ArtifactFileObject.CLASS_PREFIX + ArtifactFileObject.CLASS_SUFFIX_IN_PROGRESS, pfs);
        }

        long commandResultId = Long.parseLong(fileName.getBaseName());

        return objectFactory.buildBean(CommandResultFileObject.class, fileName, commandResultId, pfs);
    }

    protected FileType doGetType() throws Exception
    {
        return FileType.FOLDER;
    }

    protected void doAttach() throws Exception
    {
        childrenChanged(null, null);
    }

    protected String[] doListChildren() throws Exception
    {
        List<String> children = new LinkedList<String>();

        RecipeResult recipeResult = buildManager.getRecipeResult(recipeId);
        if (!recipeResult.completed())
        {
            // only look at the commands is the recipe is complete.
            return new String[]{IN_PROGRESS};
        }

        for (CommandResult commandResult : recipeResult.getCommandResults())
        {
            children.add(Long.toString(commandResult.getId()));
        }
        return children.toArray(new String[children.size()]);
    }

    public String getDisplayName()
    {
        RecipeResultNode node = buildManager.getResultNodeByResultId(recipeId);
        return String.format(STAGE_FORMAT, node.getStageName(), node.getResult().getRecipeNameSafe(), node.getAgentNameSafe());
    }

    public RecipeResult getRecipeResult()
    {
        return buildManager.getRecipeResult(getRecipeResultId());
    }

    public long getRecipeResultId()
    {
        return recipeId;
    }
}
