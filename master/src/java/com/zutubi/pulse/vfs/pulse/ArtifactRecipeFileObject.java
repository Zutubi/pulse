package com.zutubi.pulse.vfs.pulse;

import com.zutubi.pulse.core.model.CommandResult;
import com.zutubi.pulse.core.model.RecipeResult;
import com.zutubi.pulse.model.RecipeResultNode;
import org.apache.commons.vfs.FileName;
import org.apache.commons.vfs.FileType;
import org.apache.commons.vfs.provider.AbstractFileSystem;

import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;

/**
 * <class comment/>
 */
public class ArtifactRecipeFileObject extends AbstractPulseFileObject
{
    private final String STAGE_FORMAT = "build stage :: %s :: %s@%s";

    private final long recipeId;

    private String displayName;

    public ArtifactRecipeFileObject(final FileName name, final long recipeId, final AbstractFileSystem fs)
    {
        super(name, fs);
        
        this.recipeId = recipeId;
    }

    public AbstractPulseFileObject createFile(final FileName fileName) throws Exception
    {
        long commandResultId = Long.parseLong(fileName.getBaseName());

        return objectFactory.buildBean(ArtifactCommandFileObject.class,
                new Class[]{FileName.class, Long.TYPE, AbstractFileSystem.class},
                new Object[]{fileName, commandResultId, pfs}
        );
    }

    protected void doAttach() throws Exception
    {
        RecipeResultNode node = buildManager.getResultNodeByResultId(recipeId);
        displayName = String.format(STAGE_FORMAT, node.getStage(), node.getResult().getRecipeNameSafe(), node.getHostSafe());
    }

    protected FileType doGetType() throws Exception
    {
        return FileType.FOLDER;
    }

    protected String[] doListChildren() throws Exception
    {
        List<String> children = new LinkedList<String>();

        RecipeResult recipeResult = buildManager.getRecipeResult(recipeId);
        for (CommandResult commandResult : recipeResult.getCommandResults())
        {
            children.add(Long.toString(commandResult.getId()));
        }
        return children.toArray(new String[children.size()]);
    }

    protected long doGetContentSize() throws Exception
    {
        return 0;
    }

    protected InputStream doGetInputStream() throws Exception
    {
        return null;
    }

    public String getDisplayName()
    {
        return this.displayName;
    }
}
