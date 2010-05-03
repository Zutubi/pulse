package com.zutubi.pulse.master.vfs.provider.pulse;

import com.zutubi.pulse.core.model.CommandResult;
import com.zutubi.pulse.core.model.RecipeResult;
import com.zutubi.pulse.master.model.BuildResult;
import com.zutubi.pulse.master.model.RecipeResultNode;
import com.zutubi.util.CollectionUtils;
import com.zutubi.util.Mapping;
import com.zutubi.util.logging.Logger;
import org.apache.commons.vfs.FileName;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileType;
import org.apache.commons.vfs.provider.AbstractFileSystem;

import java.util.List;
import java.util.Map;

/**
 * Represents the details of a single stage in a single build - tailored to
 * provide information for the details tab.
 */
public class StageDetailsFileObject extends AbstractResultDetailsFileObject implements RecipeResultProvider
{
    private static final Logger LOG = Logger.getLogger(StageDetailsFileObject.class);
    
    public StageDetailsFileObject(final FileName name, final AbstractFileSystem fs)
    {
        super(name, fs);
    }

    public AbstractPulseFileObject createFile(final FileName fileName)
    {
        return objectFactory.buildBean(CommandDetailsFileObject.class,
                new Class[]{FileName.class, AbstractFileSystem.class},
                new Object[]{fileName, pfs}
        );
    }

    protected FileType doGetType() throws Exception
    {
        return FileType.FOLDER;
    }

    protected String[] doListChildren() throws Exception
    {
        List<CommandResult> commandResults = getRecipeResult().getCommandResults();
        return CollectionUtils.mapToArray(commandResults, new Mapping<CommandResult, String>()
        {
            public String map(CommandResult commandResult)
            {
                return commandResult.getCommandName();
            }
        }, new String[commandResults.size()]);
    }

    public RecipeResult getRecipeResult() throws FileSystemException
    {
        BuildResult result = getBuildResult();
        if (result == null)
        {
            throw new FileSystemException("No build result available.");
        }

        String stageName = getName().getBaseName();
        RecipeResultNode node = result.findResultNode(stageName);
        if (node == null)
        {
            throw new FileSystemException(String.format("No stage by the name '%s' is available.", stageName));
        }
        
        RecipeResult recipeResult = node.getResult();
        if (recipeResult == null)
        {
            throw new FileSystemException("No recipe result is available.");
        }
        
        return recipeResult;
    }

    public long getRecipeResultId() throws FileSystemException
    {
        return getRecipeResult().getId();
    }

    protected BuildResult getBuildResult() throws FileSystemException
    {
        BuildResultProvider provider = getAncestor(BuildResultProvider.class);
        if (provider == null)
        {
            throw new FileSystemException("Missing build result context.");
        }
        
        return provider.getBuildResult();
    }

    @Override
    protected RecipeResult getResult() throws FileSystemException
    {
        return getRecipeResult();
    }

    @Override
    public Map<String, Object> getExtraAttributes()
    {
        Map<String, Object> attributes = super.getExtraAttributes();
        attributes.put("stageName", getName().getBaseName());
        return attributes;
    }
}