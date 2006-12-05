package com.zutubi.pulse.vfs.pulse;

import com.zutubi.pulse.model.BuildResult;
import com.zutubi.pulse.model.RecipeResultNode;
import org.apache.commons.vfs.FileName;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileType;
import org.apache.commons.vfs.provider.AbstractFileSystem;

import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;

/**
 * <class comment/>
 */
public class WorkingCopyContextFileObject extends AbstractPulseFileObject implements AddressableFileObject
{
    public WorkingCopyContextFileObject(final FileName name, final AbstractFileSystem fs)
    {
        super(name, fs);
    }

    public AbstractPulseFileObject createFile(final FileName fileName) throws Exception
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
        List<String> recipeIds = new LinkedList<String>();
        // list the recipe ids.
        BuildResult result = getBuildResult();
        for (RecipeResultNode node : result.getRoot().getChildren())
        {
            recipeIds.add(Long.toString(node.getResult().getId()));
        }

        // can not list this node.
        return recipeIds.toArray(new String[recipeIds.size()]);
    }

    protected long doGetContentSize() throws Exception
    {
        return 0;
    }

    protected InputStream doGetInputStream() throws Exception
    {
        return null;
    }

    private BuildResult getBuildResult() throws FileSystemException
    {
        BuildResultProvider provider = (BuildResultProvider) getAncestor(BuildResultProvider.class);
        return provider.getBuildResult();
    }

    private long getBuildResultId()
    {
        try
        {
            BuildResultProvider provider = (BuildResultProvider) getAncestor(BuildResultProvider.class);
            return provider.getBuildResultId();
        }
        catch (FileSystemException e)
        {
            return -1;
        }
    }

    public String getUrlPath()
    {
        return "/browseProjectDir.action?buildId=" + getBuildResultId();   
    }
}
