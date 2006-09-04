package com.zutubi.pulse.scm;

import com.zutubi.pulse.core.model.Revision;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 */
public class WorkingCopyStatus implements Iterable<FileStatus>
{
    private Revision revision;
    private List<FileStatus> changes;

    public WorkingCopyStatus()
    {
        changes = new LinkedList<FileStatus>();
    }

    public WorkingCopyStatus(Revision revision)
    {
        this();
        this.revision = revision;
    }

    public Revision getRevision()
    {
        return revision;
    }

    public void setRevision(Revision revision)
    {
        this.revision = revision;
    }

    public void add(FileStatus status)
    {
        changes.add(status);
    }

    public Iterator<FileStatus> iterator()
    {
        return changes.iterator();
    }

    public boolean inConsistentState()
    {
        for(FileStatus fs: changes)
        {
            if(!fs.getState().isConsistent())
            {
                System.err.println(fs.getPath() +": " + fs.getState().toString());
                return false;
            }
        }

        return true;
    }
}
