package com.zutubi.pulse.master.vfs.provider.pulse;

import org.apache.commons.vfs.FileName;
import org.apache.commons.vfs.FileType;
import org.apache.commons.vfs.provider.AbstractFileSystem;

/**
 * <class comment/>
 */
public class ArtifactsFileObject extends AbstractPulseFileObject
{
    public ArtifactsFileObject(final FileName name, final AbstractFileSystem fs)
    {
        super(name, fs);
    }

    public AbstractPulseFileObject createFile(final FileName fileName)
    {
        long artifactId = Long.parseLong(fileName.getBaseName());
        return objectFactory.buildBean(ArtifactFileObject.class,
                new Class[]{FileName.class, Long.TYPE, AbstractFileSystem.class},
                new Object[]{fileName, artifactId, pfs}
        );
    }

    protected FileType doGetType() throws Exception
    {
        // this object does allow traversal.
        return FileType.FOLDER;
    }

    protected String[] doListChildren() throws Exception
    {
        // this object does not support listing.
        return NO_CHILDREN;
    }
}
