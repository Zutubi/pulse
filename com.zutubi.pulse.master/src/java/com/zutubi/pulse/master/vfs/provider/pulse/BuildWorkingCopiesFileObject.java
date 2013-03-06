package com.zutubi.pulse.master.vfs.provider.pulse;

import com.google.common.base.Function;
import com.zutubi.pulse.master.model.BuildResult;
import com.zutubi.pulse.master.model.RecipeResultNode;
import org.apache.commons.vfs.FileName;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileType;
import org.apache.commons.vfs.provider.AbstractFileSystem;

import java.util.List;

import static com.google.common.collect.Collections2.transform;

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

        List<RecipeResultNode> stages = result.getStages();
        return transform(stages, new Function<RecipeResultNode, String>()
        {
            public String apply(RecipeResultNode node)
            {
                return Long.toString(node.getResult().getId());
            }
        }).toArray(new String[stages.size()]);
    }

    private BuildResult getBuildResult() throws FileSystemException
    {
        BuildResultProvider provider = getAncestor(BuildResultProvider.class);
        return provider.getBuildResult();
    }
}
