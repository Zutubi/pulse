package com.zutubi.pulse.core.scm.api;


import java.io.File;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 */
public class WorkingCopyStatus implements Iterable<FileStatus>
{
    private List<FileStatus> changes = new LinkedList<FileStatus>();
    private transient File base;

    public WorkingCopyStatus()
    {
    }

    public WorkingCopyStatus(File base)
    {
        this.base = base;
    }

    public void add(FileStatus status)
    {
        getChanges().add(status);
    }

    public Iterator<FileStatus> iterator()
    {
        return getChanges().iterator();
    }

    public boolean inConsistentState()
    {
        for(FileStatus fs: getChanges())
        {
            if(!fs.getState().isConsistent())
            {
                return false;
            }
        }

        return true;
    }

    public List<FileStatus> getChanges()
    {
        // Odd perhaps, but being instantiated by XStream means we need to
        // guard against null (the meaning is clear: an empty list).
        if(changes == null)
        {
            changes = new LinkedList<FileStatus>();
        }
        return changes;
    }

    public FileStatus getFileStatus(String path)
    {
        for(FileStatus fs: changes)
        {
            if(fs.getPath().equals(path))
            {
                return fs;
            }
        }

        return null;
    }

    public void removeFileStatus(String path)
    {
        Iterator<FileStatus> i = changes.iterator();
        while(i.hasNext())
        {
            if(i.next().getPath().equals(path))
            {
                i.remove();
                break;
            }
        }
    }

    public boolean hasChanges()
    {
        return changes.size() > 0;
    }

    public File getBase()
    {
        return base;
    }
}
