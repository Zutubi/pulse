package com.zutubi.pulse.vfs.pulse;

import org.apache.commons.vfs.FileName;
import org.apache.commons.vfs.FileType;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.provider.AbstractFileSystem;

import java.io.InputStream;
import java.util.List;
import java.util.LinkedList;

import com.zutubi.pulse.model.RecipeResultNode;
import com.zutubi.pulse.model.BuildResult;

/**
 * <class comment/>
 */
public class ArtifactsRootFileObject extends AbstractPulseFileObject
{
    public ArtifactsRootFileObject(final FileName name, final AbstractFileSystem fs)
    {
        super(name, fs);
    }

    public AbstractPulseFileObject createFile(final FileName fileName) throws Exception
    {
        // this is a recipe node.
        long recipeNodeId = Long.parseLong(fileName.getBaseName());
        return objectFactory.buildBean(ArtifactRecipeFileObject.class,
                new Class[]{FileName.class, Long.TYPE, AbstractFileSystem.class},
                new Object[]{fileName, recipeNodeId, pfs}
        );
    }

    protected FileType doGetType() throws Exception
    {
        return FileType.FOLDER;
    }

    protected String[] doListChildren() throws Exception
    {
        // return a list of the recipes.
        List<String> results = new LinkedList<String>();

        RecipeResultNode node = getBuildResult().getRoot();
        for (final RecipeResultNode child : node.getChildren())
        {
            results.add(String.format("%s", child.getId()));
        }
        return results.toArray(new String[results.size()]);
    }

    protected long doGetContentSize() throws Exception
    {
        return 0;
    }

    protected InputStream doGetInputStream() throws Exception
    {
        return null;
    }

    protected BuildResult getBuildResult() throws FileSystemException
    {
        BuildResultNode node = (BuildResultNode) getAncestor(BuildResultNode.class);
        if (node != null)
        {
            return node.getBuildResult();
        }
        return null;
    }
}
