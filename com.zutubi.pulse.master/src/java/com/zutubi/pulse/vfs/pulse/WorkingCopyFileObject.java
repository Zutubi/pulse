package com.zutubi.pulse.vfs.pulse;

import com.zutubi.pulse.model.Project;
import com.zutubi.pulse.vfs.FileAction;
import org.apache.commons.vfs.FileName;
import org.apache.commons.vfs.FileSystemException;
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
public class WorkingCopyFileObject extends AbstractPulseFileObject implements AddressableFileObject
{
    private final File base;

    public WorkingCopyFileObject(final FileName name, final File base, final AbstractFileSystem fs)
    {
        super(name, fs);

        this.base = base;
    }

    public AbstractPulseFileObject createFile(final FileName fileName) throws Exception
    {
        File newBase = new File(base, fileName.getBaseName());
        return objectFactory.buildBean(WorkingCopyFileObject.class,
                new Class[]{FileName.class, File.class, AbstractFileSystem.class},
                new Object[]{fileName, newBase, pfs}
        );
    }

    protected FileType doGetType() throws Exception
    {
        if (base.isDirectory())
        {
            return FileType.FOLDER;
        }
        return FileType.FILE;
    }

    public List<FileAction> getActions()
    {
        if (base.isFile())
        {
            List<FileAction> actions = new LinkedList<FileAction>();
            actions.add(new FileAction("download", getUrlPath()));
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

    public boolean isLocal()
    {
        return true;
    }

    public String getUrlPath()
    {
        try
        {
            return "/file/builds/" + getBuildId() + "/wc/" + getRecipeId() + "/" + getWorkingCopyPath();
        }
        catch (FileSystemException e)
        {
            e.printStackTrace();
            return "";
        }
    }

    private long getBuildId() throws FileSystemException
    {
        BuildResultProvider provider = getAncestor(BuildResultProvider.class);
        return provider.getBuildResultId();
    }

    private long getRecipeId() throws FileSystemException
    {
        RecipeResultProvider provider = getAncestor(RecipeResultProvider.class);
        return provider.getRecipeResultId();
    }

    protected String getWorkingCopyPath() throws FileSystemException
    {
        AbstractPulseFileObject fo = getAncestor(WorkingCopyStageFileObject.class);
        return fo.getName().getRelativeName(getName());
    }

    protected boolean doIsReadable() throws Exception
    {
        try
        {
            ProjectProvider node = getAncestor(ProjectProvider.class);
            Project project = node.getProject();
            projectManager.checkWrite(project);
            return true;
        }
        catch (FileSystemException e)
        {
            throw e;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return false;
        }
    }
}
