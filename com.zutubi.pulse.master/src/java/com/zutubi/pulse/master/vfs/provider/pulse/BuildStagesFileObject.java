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
 * Represents the stages within a build.
 */
public class BuildStagesFileObject extends AbstractPulseFileObject
{
    public BuildStagesFileObject(final FileName name, final AbstractFileSystem fs)
    {
        super(name, fs);
    }

    public AbstractPulseFileObject createFile(final FileName fileName) throws FileSystemException
    {
        return objectFactory.buildBean(NamedStageFileObject.class,
                new Class[]{FileName.class, String.class, AbstractFileSystem.class},
                new Object[]{fileName, fileName.getBaseName(), pfs}
        );
    }

    protected FileType doGetType() throws Exception
    {
        return FileType.FOLDER;
    }

    protected String[] doListChildren() throws Exception
    {
        BuildResultProvider provider = getAncestor(BuildResultProvider.class);
        if (provider != null)
        {
            BuildResult result = provider.getBuildResult();
            if (result != null)
            {
                List<RecipeResultNode> nodes = result.getStages();
                return transform(nodes, new Function<RecipeResultNode, String>()
                {
                    public String apply(RecipeResultNode recipeResultNode)
                    {
                        return recipeResultNode.getStageName();
                    }
                }).toArray(new String[nodes.size()]);
            }
        }

        return NO_CHILDREN;
    }
}