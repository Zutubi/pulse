package com.zutubi.pulse.master.vfs.provider.pulse;

import com.zutubi.pulse.master.webwork.Urls;
import org.apache.commons.vfs.FileName;
import org.apache.commons.vfs.FileType;
import org.apache.commons.vfs.provider.AbstractFileSystem;

/**
 */
public class ProjectConfigsFileObject extends AbstractPulseFileObject implements AddressableFileObject
{
    public ProjectConfigsFileObject(final FileName name, final AbstractFileSystem fs)
    {
        super(name, fs);
    }

    public AbstractPulseFileObject createFile(FileName fileName)
    {
        return objectFactory.buildBean(ProjectConfigFileObject.class,
                new Class[]{FileName.class, AbstractFileSystem.class},
                new Object[]{fileName, pfs});
    }

    protected FileType doGetType() throws Exception
    {
        // allow traversal of this node.
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

    public String getUrlPath()
    {
        return Urls.getBaselessInstance().projects();
    }
}
