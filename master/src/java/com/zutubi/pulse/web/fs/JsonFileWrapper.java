package com.zutubi.pulse.web.fs;

import com.zutubi.pulse.util.FileSystemUtils;

import java.io.File;

/**
 * <class-comment/>
 */
public class JsonFileWrapper
{
    private final File file;

    public JsonFileWrapper(File f)
    {
        this.file = f;
    }

    public String getName()
    {
        if (FileSystemUtils.isRoot(this.file))
        {
            return this.file.getAbsolutePath();
        }
        return this.file.getName();
    }

    public String getSeparator()
    {
        return File.separator;
    }

    public String getType()
    {
        if (FileSystemUtils.isRoot(file))
        {
            return "root";
        }
        else if (file.isDirectory())
        {
            return "folder";
        }
        else
        {
            return "file";
        }
    }

}
