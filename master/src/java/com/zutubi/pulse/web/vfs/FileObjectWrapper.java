package com.zutubi.pulse.web.vfs;

import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileType;
import org.apache.commons.vfs.FileSystemException;

import java.io.File;
import java.util.List;
import java.util.Collections;

import com.zutubi.pulse.vfs.pulse.PulseFileObject;

/**
 * <class comment/>
 */
public class FileObjectWrapper
{
    private FileObject fo;

    public FileObjectWrapper(FileObject fo)
    {
        this.fo = fo;
    }

    /**
     *
     */
    public String getName()
    {
        if (fo instanceof PulseFileObject)
        {
            return ((PulseFileObject)fo).getDisplayName();
        }
        return fo.getName().getBaseName();
    }

    /**
     *
     */
    public String getType()
    {
        try
        {
            FileType type = fo.getType();
            if (type == FileType.FOLDER)
            {
                return "folder";
            }
            if (type == FileType.FILE)
            {
                return "file";
            }
            return "root";
        }
        catch (FileSystemException e)
        {
            e.printStackTrace();
            return "unknown";
        }
    }

    /**
     *
     */
    public String getId()
    {
        return fo.getName().getBaseName();
    }

    /**
     *
     */
    public boolean isContainer()
    {
        try
        {
            return fo.getType() == FileType.FOLDER;
        }
        catch (FileSystemException e)
        {
            return false;
        }
    }

    public String getSeparator()
    {
        return File.separator;
    }

    public List<String> getActions()
    {
        if (fo instanceof PulseFileObject)
        {
            return ((PulseFileObject)fo).getActions();
        }
        return Collections.EMPTY_LIST;
    }
}
