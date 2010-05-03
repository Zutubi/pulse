package com.zutubi.pulse.master.vfs.provider.pulse;

import com.zutubi.pulse.master.model.BuildResult;
import com.zutubi.pulse.master.model.RecipeResultNode;
import com.zutubi.util.CollectionUtils;
import com.zutubi.util.Mapping;
import org.apache.commons.vfs.FileName;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileType;
import org.apache.commons.vfs.provider.AbstractFileSystem;

import java.util.List;

/**
 * Represents the working copies for a build, keyed by stage.
 */
public class BuildWorkingCopiesFileObject extends AbstractPulseFileObject
{
    public BuildWorkingCopiesFileObject(final FileName name, final AbstractFileSystem fs)
    {
        super(name, fs);
    }

    public AbstractPulseFileObject createFile(final FileName fileName)
    {
        long recipeId = Long.parseLong(fileName.getBaseName());

        return objectFactory.buildBean(WorkingCopyStageFileObject.class,
                new Class[]{FileName.class, Long.TYPE, AbstractFileSystem.class},
                new Object[]{fileName, recipeId, pfs}
        );
    }

    protected FileType doGetType() throws Exception
    {
        // can traverse this node.
        return FileType.FOLDER;
    }

    protected String[] doListChildren() throws Exception
    {
        BuildResult result = getBuildResult();
        if (result == null)
        {
            throw new FileSystemException("buildResult.not.available");
        }

        List<RecipeResultNode> stages = result.getRoot().getChildren();
        return CollectionUtils.mapToArray(stages, new Mapping<RecipeResultNode, String>()
        {
            public String map(RecipeResultNode node)
            {
                return Long.toString(node.getResult().getId());
            }
        }, new String[stages.size()]);
    }

    private BuildResult getBuildResult() throws FileSystemException
    {
        BuildResultProvider provider = getAncestor(BuildResultProvider.class);
        return provider.getBuildResult();
    }
}
