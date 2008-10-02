package com.zutubi.pulse.master.vfs.pulse;

import com.zutubi.tove.type.ComplexType;
import com.zutubi.tove.type.record.PathUtils;
import com.zutubi.tove.type.record.Record;
import com.zutubi.tove.webwork.ToveUtils;
import org.apache.commons.vfs.FileName;
import org.apache.commons.vfs.FileType;
import org.apache.commons.vfs.provider.AbstractFileSystem;

/**
 * A file to represent the root of the server configuration.  Descendents of
 * this file represent paths into the config system.
 */
public class ConfigErrorFileObject extends AbstractPulseFileObject
{
    /**
     * This is the path into the configuration subsystem.
     */
    private String path;
    private ComplexType parentType;
    private ComplexType type;
    private Record value;
    private String error;

    public ConfigErrorFileObject(FileName name, AbstractFileSystem fs)
    {
        super(name, fs);
        path = "";
        parentType = null;
        type = null;
        value = null;
        error = null;
    }

    public ConfigErrorFileObject(FileName name, AbstractFileSystem fs, String path, ComplexType parentType, ComplexType type, Record value, String error)
    {
        super(name, fs);
        this.path = path;
        this.parentType = parentType;
        this.type = type;
        this.value = value;
        this.error = error;
    }

    public AbstractPulseFileObject createFile(final FileName fileName) throws Exception
    {
        return null;
    }

    public String getDisplayName()
    {
        if (type == null || value == null)
        {
            // Sometimes we have little to go on...
            return PathUtils.getBaseName(path);
        }
        else
        {
            return ToveUtils.getDisplayName(path, type, parentType, value);
        }
    }

    protected FileType doGetType() throws Exception
    {
        return FileType.FILE;
    }

    public String getCls()
    {
        return null;
    }

    public String getIconCls()
    {
        if (type == null)
        {
            return "config-error-icon";
        }
        else
        {
            return ToveUtils.getIconCls(type);
        }
    }

    protected String[] doListChildren() throws Exception
    {
        return new String[0];
    }
}
