package com.zutubi.pulse.master.vfs.provider.pulse;

import org.apache.commons.vfs.FileName;
import org.apache.commons.vfs.FileType;
import org.apache.commons.vfs.provider.AbstractFileSystem;

/**
 * A file object that can be used to insert an arbitrary text message into a
 * tree, with an arbitrary icon.
 */
public class TextMessageFileObject extends AbstractPulseFileObject
{
    private final String iconCls;

    public TextMessageFileObject(final FileName name, final String iconCls, final AbstractFileSystem fs)
    {
        super(name, fs);
        this.iconCls = iconCls;
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

    public String getIconCls()
    {
        return iconCls;
    }
}
