package com.zutubi.pulse.web.vfs;

import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileType;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileName;

import java.io.File;
import java.util.List;
import java.util.Collections;

import com.zutubi.pulse.vfs.pulse.AbstractPulseFileObject;
import com.zutubi.pulse.vfs.pulse.AddressableFileObject;
import com.zutubi.pulse.vfs.pulse.FileTypeConstants;
import com.zutubi.pulse.util.logging.Logger;

/**
 * <class comment/>
 */
public class FileObjectWrapper
{
    private static final Logger LOG = Logger.getLogger(FileObjectWrapper.class);

    private FileObject fo;
    private FileObject base;

    public FileObjectWrapper(FileObject fo, FileObject base)
    {
        this.fo = fo;
        this.base = base;
    }

    /**
     *
     */
    public String getName()
    {
        if (fo instanceof AbstractPulseFileObject)
        {
            return ((AbstractPulseFileObject)fo).getDisplayName();
        }
        return fo.getName().getBaseName();
    }

    public String getUrl()
    {
        try
        {
            if (fo instanceof AddressableFileObject)
            {
                return ((AddressableFileObject)fo).getUrlPath();
            }
        }
        catch (FileSystemException e)
        {
            //noop.
        }
        return "";
    }

    /**
     *
     */
    public String getType()
    {
        try
        {
            if (fo instanceof AbstractPulseFileObject)
            {
                return ((AbstractPulseFileObject)fo).getFileType();
            }
            else
            {
                FileType type = fo.getType();
                if (type == FileType.FOLDER)
                {
                    return FileTypeConstants.FOLDER;
                }
                if (type == FileType.FILE)
                {
                    return FileTypeConstants.FILE;
                }
                return FileTypeConstants.UNKNOWN;
            }
        }
        catch (FileSystemException e)
        {
            LOG.warning(e);
            return FileTypeConstants.UNKNOWN;
        }
    }

    /**
     *
     */
    public String getId()
    {
        return fo.getName().getBaseName();
    }

    public String getRelativeParentPath()
    {
        try
        {
            FileObject parent = fo.getParent();
            if(parent != null)
            {
                String path = base.getName().getRelativeName(parent.getName());
                if(path.equals("."))
                {
                    path = "";
                }
                return path;
            }
        }
        catch (FileSystemException e)
        {
            LOG.severe(e);
        }
        return "";
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
        if (fo instanceof AbstractPulseFileObject)
        {
            return ((AbstractPulseFileObject)fo).getActions();
        }
        return Collections.EMPTY_LIST;
    }
}
