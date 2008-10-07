package com.zutubi.pulse.core.scm;

import com.zutubi.pulse.core.model.Change;
import com.zutubi.pulse.core.scm.api.ScmCancelledException;
import com.zutubi.pulse.core.scm.api.ScmEventHandler;

import java.util.LinkedList;
import java.util.List;

/**
 * 
 */
public class ScmChangeAccumulator implements ScmEventHandler
{
    List<Change> changes = new LinkedList<Change>();

    public List<Change> getChanges()
    {
        return changes;
    }

    public void status(String message)
    {
    }

    public void fileChanged(Change change)
    {
        changes.add(change);
    }

    public void checkCancelled() throws ScmCancelledException
    {
    }
}
