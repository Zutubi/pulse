package com.zutubi.pulse.master.vfs.provider.pulse;

import org.apache.commons.vfs.FileName;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileType;
import org.apache.commons.vfs.provider.AbstractFileSystem;

/**
 * <class comment/>
 */
public class TextMessageFileObject extends AbstractPulseFileObject
{
    private final String type;

    public TextMessageFileObject(final FileName name, final String type, final AbstractFileSystem fs)
    {
        super(name, fs);
        this.type = type;
    }

    public AbstractPulseFileObject createFile(final FileName fileName) throws Exception
    {
        return null;
    }

    protected FileType doGetType() throws Exception
    {
        return FileType.FILE;
    }

    protected String[] doListChildren() throws Exception
    {
        return new String[0];
    }

    public String getFileType() throws FileSystemException
    {
        return type;
    }
}
