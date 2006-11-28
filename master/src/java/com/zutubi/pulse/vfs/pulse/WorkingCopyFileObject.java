package com.zutubi.pulse.vfs.pulse;

import com.zutubi.pulse.model.RecipeResultNode;
import org.apache.commons.vfs.FileName;
import org.apache.commons.vfs.FileType;
import org.apache.commons.vfs.provider.AbstractFileSystem;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;

/**
 * <class comment/>
 */
public class WorkingCopyFileObject extends AbstractPulseFileObject
{
    private final String STAGE_FORMAT = "stage :: %s :: %s@%s";

    private final File base;

    private long recipeNodeId = -1;
    private String displayName;

    public WorkingCopyFileObject(final FileName name, final File base, final AbstractFileSystem fs)
    {
        super(name, fs);

        this.base = base;
    }

    public void setRecipeNodeId(long recipeNodeId)
    {
        this.recipeNodeId = recipeNodeId;
    }

    protected void doAttach() throws Exception
    {
        if (recipeNodeId != -1)
        {
            RecipeResultNode node = pfs.getBuildManager().getRecipeResultNode(recipeNodeId);
            displayName = String.format(STAGE_FORMAT, node.getStage(), node.getResult().getRecipeNameSafe(), node.getHostSafe());
        }
        else
        {
            displayName = base.getName();
        }
    }

    public String getDisplayName()
    {
        return this.displayName;
    }

    public AbstractPulseFileObject createFile(final FileName fileName) throws Exception
    {
        File newBase = new File(base, fileName.getBaseName());
        return new WorkingCopyFileObject(fileName, newBase, pfs);
    }

    protected FileType doGetType() throws Exception
    {
        if (base.isDirectory())
        {
            return FileType.FOLDER;
        }
        if (base.isFile())
        {
            return FileType.FILE;
        }
        return null;
    }

    public List<String> getActions()
    {
        if (base.isFile())
        {
            List<String> actions = new LinkedList<String>();
            actions.add("download");
            return actions;
        }
        return super.getActions();
    }
    
    protected String[] doListChildren() throws Exception
    {
        return base.list();
    }

    protected long doGetContentSize() throws Exception
    {
        return base.length();
    }

    protected InputStream doGetInputStream() throws Exception
    {
        return new FileInputStream(base);
    }

/*
    protected boolean doIsReadable() throws Exception
    {
        try
        {
            ProjectNode node = (ProjectNode) getAncestor(ProjectNode.class);
            Project project = node.getProject();
            pfs.getProjectManager().checkWrite(project);
            return true;
        }
        catch (FileSystemException e)
        {
            throw e;
        }
        catch (Exception e)
        {
            return false;
        }
    }
*/
}
