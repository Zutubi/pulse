package com.zutubi.pulse.master.vfs.pulse;

import org.apache.commons.vfs.FileName;
import org.apache.commons.vfs.FileType;
import org.apache.commons.vfs.provider.AbstractFileSystem;

/**
 * <class comment/>
 */
public class NamedArtifactsFileObject extends AbstractPulseFileObject
{
    public NamedArtifactsFileObject(final FileName name, final AbstractFileSystem fs)
    {
        super(name, fs);
    }

    public AbstractPulseFileObject createFile(final FileName fileName) throws Exception
    {
        String artifactName = fileName.getBaseName();
        return objectFactory.buildBean(NamedArtifactFileObject.class,
                new Class[]{FileName.class, String.class, AbstractFileSystem.class},
                new Object[]{fileName, artifactName, pfs}
        );
    }

    protected FileType doGetType() throws Exception
    {
        return FileType.IMAGINARY;
    }

    protected String[] doListChildren() throws Exception
    {
        // do not list
        return new String[0];
    }
}
