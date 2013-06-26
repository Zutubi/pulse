package com.zutubi.pulse.master.vfs.provider.pulse;

import org.apache.commons.vfs.FileName;
import org.apache.commons.vfs.FileType;
import org.apache.commons.vfs.provider.AbstractFileSystem;

/**
 */
public class ProjectWizardsFileObject extends AbstractPulseFileObject
{
    public ProjectWizardsFileObject(final FileName name, final AbstractFileSystem fs)
    {
        super(name, fs);
    }

    public AbstractPulseFileObject createFile(FileName fileName)
    {
        // the fileName is the name of the parent project
        return objectFactory.buildBean(ProjectWizardFileObject.class, fileName, pfs);
    }

    protected FileType doGetType() throws Exception
    {
        return FileType.FOLDER;
    }

    protected String[] doListChildren() throws Exception
    {
        // do not support listing.
        return NO_CHILDREN;
    }

    public boolean isLocal()
    {
        return true;
    }
}
