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
public class ArtifactsContextFileObject extends AbstractPulseFileObject implements AddressableFileObject
{
    public ArtifactsContextFileObject(final FileName name, final AbstractFileSystem fs)
    {
        super(name, fs);
    }

    public AbstractPulseFileObject createFile(final FileName fileName) throws Exception
    {
        // this is a recipe node.
        long recipeId = Long.parseLong(fileName.getBaseName());
        return objectFactory.buildBean(ArtifactStageFileObject.class,
                new Class[]{FileName.class, Long.TYPE, AbstractFileSystem.class},
                new Object[]{fileName, recipeId, pfs}
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

        BuildResult result = getBuildResult();
        if (result != null)
        {
            RecipeResultNode node = getBuildResult().getRoot();
            for (final RecipeResultNode child : node.getChildren())
            {
                results.add(Long.toString(child.getResult().getId()));
            }
            return results.toArray(new String[results.size()]);
        }

        return new String[0];
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
        BuildResultProvider provider = (BuildResultProvider) getAncestor(BuildResultProvider.class);
        if (provider != null)
        {
            return provider.getBuildResult();
        }
        return null;
    }

    protected long getBuildResultId()
    {
        try
        {
            BuildResultProvider provider = (BuildResultProvider) getAncestor(BuildResultProvider.class);
            if (provider != null)
            {
                return provider.getBuildResultId();
            }
            return -1;
        }
        catch (FileSystemException e)
        {
            return -1;
        }
    }

    public String getUrlPath()
    {
        return "/viewBuildArtifacts.action?id=" + getBuildResultId();
    }
}
