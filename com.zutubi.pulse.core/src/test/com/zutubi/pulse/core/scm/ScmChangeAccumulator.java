package com.zutubi.pulse.core.scm;

import com.zutubi.pulse.core.scm.api.FileChange;
import com.zutubi.pulse.core.scm.api.ScmCancelledException;
import com.zutubi.pulse.core.scm.api.ScmEventHandler;

import java.util.LinkedList;
import java.util.List;

/**
 * 
 */
public class ScmChangeAccumulator implements ScmEventHandler
{
    List<FileChange> changes = new LinkedList<FileChange>();

    public List<FileChange> getChanges()
    {
        return changes;
    }

    public void status(String message)
    {
    }

    public void fileChanged(FileChange change)
    {
        changes.add(change);
    }

    public void checkCancelled() throws ScmCancelledException
    {
    }
}
