package com.zutubi.pulse.vfs.pulse;

import com.zutubi.pulse.model.RecipeResultNode;
import com.zutubi.pulse.model.BuildResult;
import org.apache.commons.vfs.FileName;
import org.apache.commons.vfs.FileType;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.provider.AbstractFileSystem;

import java.io.File;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;

/**
 * <class comment/>
 */
public class WorkingCopyRootFileObject extends AbstractPulseFileObject
{
    public WorkingCopyRootFileObject(final FileName name, final AbstractFileSystem fs)
    {
        super(name, fs);
    }

    public AbstractPulseFileObject createFile(final FileName fileName) throws Exception
    {
        // this is a recipe node.
        long recipeNodeId = Long.parseLong(fileName.getBaseName());

        long recipeId = pfs.getBuildManager().getRecipeResultNode(recipeNodeId).getResult().getId();

        // find the base.
        File base = ((PulseFileSystem)getFileSystem()).getBaseDir(getBuildResultId(), recipeId);

        WorkingCopyFileObject fo = objectFactory.buildBean(WorkingCopyFileObject.class,
                new Class[]{FileName.class, File.class, AbstractFileSystem.class},
                new Object[]{fileName, base, pfs}
        );
        fo.setRecipeNodeId(recipeNodeId);
        return fo;
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

    protected long getBuildResultId() throws FileSystemException
    {
        BuildResultNode node = (BuildResultNode) getAncestor(BuildResultNode.class);
        if (node != null)
        {
            return node.getBuildResultId();
        }
        return -1;
    }
}
