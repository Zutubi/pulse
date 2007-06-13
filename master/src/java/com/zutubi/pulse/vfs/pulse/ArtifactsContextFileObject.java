package com.zutubi.pulse.vfs.pulse;

import com.zutubi.pulse.model.BuildResult;
import com.zutubi.pulse.model.RecipeResultNode;
import org.apache.commons.vfs.FileName;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileType;
import org.apache.commons.vfs.provider.AbstractFileSystem;

import java.util.LinkedList;
import java.util.List;

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
        // Should refer to a stage.  First, try to interpret the name as a
        // recipe result ID.  If that doesn't work, try looking at it as a
        // stage name.
        try
        {
            long recipeId = Long.parseLong(fileName.getBaseName());
            if(buildManager.getRecipeResult(recipeId) != null)
            {
                return objectFactory.buildBean(ArtifactStageFileObject.class,
                                               new Class[]{FileName.class, Long.TYPE, AbstractFileSystem.class},
                                               new Object[]{fileName, recipeId, pfs}
                );
            }
        }
        catch (NumberFormatException e)
        {
            // Maybe a stage name, continue the research Smithers.
        }
        
        return objectFactory.buildBean(NamedStageFileObject.class,
                new Class[]{FileName.class, String.class, AbstractFileSystem.class},
                new Object[]{fileName, fileName.getBaseName(), pfs}
        );
    }

    protected FileType doGetType() throws Exception
    {
        return FileType.FOLDER;
    }

    protected void doAttach() throws Exception
    {
        childrenChanged(null, null);
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

    protected BuildResult getBuildResult() throws FileSystemException
    {
        BuildResultProvider provider = getAncestor(BuildResultProvider.class);
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
            BuildResultProvider provider = getAncestor(BuildResultProvider.class);
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

    public boolean isLocal()
    {
        return true;
    }

    public String getUrlPath()
    {
        return "/viewBuildArtifacts.action?id=" + getBuildResultId();
    }
}
