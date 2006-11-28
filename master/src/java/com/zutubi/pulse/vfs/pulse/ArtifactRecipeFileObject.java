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

    private final long recipeNodeId;

    private String displayName;
    private RecipeResultNode node;

    public ArtifactRecipeFileObject(final FileName name, final long recipeNodeId, final AbstractFileSystem fs)
    {
        super(name, fs);
        
        this.recipeNodeId = recipeNodeId;
    }

    public AbstractPulseFileObject createFile(final FileName fileName) throws Exception
    {
        long commandResultId = Long.parseLong(fileName.getBaseName());
        return new ArtifactCommandFileObject(fileName, commandResultId, pfs);
    }

    protected void doAttach() throws Exception
    {
        node = pfs.getBuildManager().getRecipeResultNode(recipeNodeId);
        displayName = String.format(STAGE_FORMAT, node.getStage(), node.getResult().getRecipeNameSafe(), node.getHostSafe());
    }

    protected FileType doGetType() throws Exception
    {
        return FileType.FOLDER;
    }

    protected String[] doListChildren() throws Exception
    {
        List<String> children = new LinkedList<String>();

        RecipeResult recipeResult = node.getResult();
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
        if (this.displayName != null)
        {
            return this.displayName;
        }
        return super.getDisplayName();
    }
}
