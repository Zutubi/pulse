package com.zutubi.pulse.master.vfs.provider.pulse.reference;

import com.zutubi.pulse.master.vfs.provider.pulse.AbstractPulseFileObject;
import org.apache.commons.vfs.FileName;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.provider.AbstractFileSystem;

/**
 */
public class StaticReferenceFileObject extends AbstractReferenceFileObject
{
    public StaticReferenceFileObject(FileName fileName, AbstractFileSystem fileSystem)
    {
        super(fileName, fileSystem);
    }

    @Override
    protected String[] getDynamicChildren() throws FileSystemException
    {
        return new String[0];
    }

    @Override
    public AbstractPulseFileObject createDynamicFile(FileName fileName)
    {
        return null;
    }

    @Override
    public String getIconCls()
    {
        return "reference-static-icon";
    }

    @Override
    public String getDisplayName()
    {
        return getName().getBaseName().replace('-', ' ');
    }
}
