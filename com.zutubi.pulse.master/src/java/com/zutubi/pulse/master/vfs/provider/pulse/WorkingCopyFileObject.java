package com.zutubi.pulse.master.vfs.provider.pulse;

import com.zutubi.pulse.master.model.Project;
import com.zutubi.pulse.master.tove.config.project.ProjectConfigurationActions;
import com.zutubi.tove.security.AccessManager;
import com.zutubi.util.logging.Logger;
import org.apache.commons.vfs.FileName;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileType;
import org.apache.commons.vfs.provider.AbstractFileSystem;
import org.apache.commons.vfs.provider.UriParser;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;

/**
 */
public class WorkingCopyFileObject extends AbstractPulseFileObject implements AddressableFileObject
{
    private static final Logger LOG = Logger.getLogger(WorkingCopyFileObject.class);

    private final File base;
    private AccessManager accessManager;

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
            actions.add(new FileAction(FileAction.TYPE_DOWNLOAD, getUrlPath()));
            return actions;
        }
        return super.getActions();
    }
    
    protected String[] doListChildren() throws Exception
    {
        return UriParser.encode(base.list());
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
            LOG.warning(e);
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
        return UriParser.decode(fo.getName().getRelativeName(getName()));
    }

    protected boolean doIsReadable() throws Exception
    {
        try
        {
            ProjectProvider node = getAncestor(ProjectProvider.class);
            Project project = node.getProject();
            accessManager.ensurePermission(ProjectConfigurationActions.ACTION_VIEW_SOURCE, project);
            return true;
        }
        catch (FileSystemException e)
        {
            throw e;
        }
        catch (Exception e)
        {
            LOG.warning(e);
            return false;
        }
    }

    public void setAccessManager(AccessManager accessManager)
    {
        this.accessManager = accessManager;
    }
}
