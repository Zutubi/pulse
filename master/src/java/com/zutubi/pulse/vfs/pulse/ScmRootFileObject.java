package com.zutubi.pulse.vfs.pulse;

import com.zutubi.pulse.core.scm.config.ScmConfiguration;
import org.apache.commons.vfs.FileName;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileType;
import org.apache.commons.vfs.provider.AbstractFileSystem;

/**
 * Used to browse a Pulse project's SCM.  This file object represents the
 * root of the SCM view.
 */
public class ScmRootFileObject extends AbstractPulseFileObject implements ScmProvider
{
    public ScmRootFileObject(final FileName name, final AbstractFileSystem fs)
    {
        super(name, fs);
    }

    public AbstractPulseFileObject createFile(final FileName fileName) throws Exception
    {
        return objectFactory.buildBean(ScmFileObject.class,
                                       new Class[]{FileName.class, AbstractFileSystem.class},
                                       new Object[]{fileName, pfs});
    }

    protected FileType doGetType() throws Exception
    {
        return FileType.FOLDER;
    }

    protected String[] doListChildren() throws Exception
    {
        return new String[]{"scm"};
    }

    public ScmConfiguration getScm() throws FileSystemException
    {
        ProjectProvider projectProvider = getAncestor(ProjectProvider.class);
        return projectProvider.getProjectConfig().getScm();
    }
}
