package com.zutubi.pulse.vfs.pulse;

import org.apache.commons.vfs.provider.AbstractFileName;
import org.apache.commons.vfs.FileType;
import org.apache.commons.vfs.FileName;

/**
 * <class comment/>
 */
public class PulseFileName extends AbstractFileName
{
    public PulseFileName(final String scheme, final String absPath, FileType type)
    {
        super(scheme, absPath, type);
    }

    public FileName createName(String absPath, FileType type)
    {
        return new PulseFileName(getScheme(), absPath, type);
    }

    protected void appendRootUri(StringBuffer buffer, boolean addPassword)
    {
        buffer.append(getScheme());
        buffer.append("://");
    }
}