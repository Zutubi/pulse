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
                List<RecipeResultNode> nodes = result.getRoot().getChildren();
                return CollectionUtils.mapToArray(nodes, new Mapping<RecipeResultNode, String>()
                {
                    public String map(RecipeResultNode recipeResultNode)
                    {
                        return recipeResultNode.getStageName();
                    }
                }, new String[nodes.size()]);
            }
        }

        return NO_CHILDREN;
    }
}