package com.zutubi.pulse.vfs.pulse;

import org.apache.commons.vfs.FileName;
import org.apache.commons.vfs.FileSystem;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.provider.AbstractFileObject;
import org.apache.commons.vfs.provider.AbstractFileSystem;

import java.util.List;
import java.util.LinkedList;
import java.io.File;

/**
 * <class comment/>
 */
public abstract class PulseFileObject extends AbstractFileObject
{
    protected PulseFileSystem pfs;

    public PulseFileObject(final FileName name, final AbstractFileSystem fs)
    {
        super(name, fs);
        
        this.pfs = (PulseFileSystem) fs;
    }

    public abstract PulseFileObject createFile(final FileName fileName) throws Exception;

    public boolean canBrowse()
    {
        return true;
    }

    public boolean canView()
    {
        return true;
    }

    public String getDisplayName()
    {
        return getName().getBaseName();
    }

    public List<String> getActions()
    {
        return new LinkedList<String>();
    }

    public File getBase()
    {
        return null;
    }

    public Object getAncestor(Class type) throws FileSystemException
    {
        if (type.isAssignableFrom(this.getClass()))
        {
            return this;
        }
        PulseFileObject parent = (PulseFileObject) getParent();
        if (parent != null)
        {
            return parent.getAncestor(type);
        }
        return null;
    }
}
