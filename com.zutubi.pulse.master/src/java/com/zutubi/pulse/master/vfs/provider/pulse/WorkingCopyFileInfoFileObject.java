package com.zutubi.pulse.master.vfs.provider.pulse;

import com.zutubi.pulse.master.model.Project;
import com.zutubi.pulse.master.tove.config.project.ProjectConfigurationActions;
import com.zutubi.pulse.master.vfs.provider.pulse.file.FileInfoFileObject;
import com.zutubi.pulse.master.vfs.provider.pulse.file.FileInfoProvider;
import com.zutubi.pulse.servercore.filesystem.FileInfo;
import com.zutubi.util.logging.Logger;
import org.apache.commons.vfs.FileName;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.provider.AbstractFileSystem;

/**
 * A file object that represents a file within a working copy listing.
 */
public class WorkingCopyFileInfoFileObject extends FileInfoFileObject
{
    private static final Logger LOG = Logger.getLogger(WorkingCopyFileInfoFileObject.class);

    public WorkingCopyFileInfoFileObject(FileInfo fileInfo, FileName name, AbstractFileSystem fs)
    {
        super(fileInfo, name, fs);
    }

    public AbstractPulseFileObject createFile(final FileName fileName) throws Exception
    {
        FileInfoProvider provider = getAncestor(FileInfoProvider.class);
        String relativePath = ((FileObject)provider).getName().getRelativeName(fileName);

        FileInfo child = provider.getFileInfo(relativePath);

        return objectFactory.buildBean(WorkingCopyFileInfoFileObject.class, child, fileName, pfs);
    }

    protected boolean doIsReadable() throws Exception
    {
        try
        {
            ProjectProvider provider = getAncestor(ProjectProvider.class);
            if (provider != null)
            {
                Project project = provider.getProject();
                accessManager.ensurePermission(ProjectConfigurationActions.ACTION_VIEW_SOURCE, project);
            }
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
}
