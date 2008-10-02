package com.zutubi.pulse.master.vfs.pulse;

import com.zutubi.pulse.master.model.BuildResult;
import com.zutubi.pulse.master.model.RecipeResultNode;
import com.zutubi.pulse.master.webwork.mapping.Urls;
import org.apache.commons.vfs.FileName;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileType;
import org.apache.commons.vfs.provider.AbstractFileSystem;

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

    private BuildResult getBuildResult() throws FileSystemException
    {
        BuildResultProvider provider = getAncestor(BuildResultProvider.class);
        return provider.getBuildResult();
    }

    public boolean isLocal()
    {
        return true;
    }

    public String getUrlPath() throws FileSystemException
    {
        return new Urls("").buildWorkingCopy(getBuildResult());
    }
}
