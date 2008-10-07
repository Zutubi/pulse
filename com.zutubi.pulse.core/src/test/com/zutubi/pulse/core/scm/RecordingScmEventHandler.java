package com.zutubi.pulse.core.scm;

import com.zutubi.pulse.core.scm.api.FileChange;
import com.zutubi.pulse.core.scm.api.ScmCancelledException;
import com.zutubi.pulse.core.scm.api.ScmEventHandler;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;


/**
 */
public class RecordingScmEventHandler implements ScmEventHandler
{
    private List<FileChange> changes = new LinkedList<FileChange>();

    private List<String> statusMessages = new LinkedList<String>();

    public void fileChanged(FileChange change)
    {
        changes.add(change);
    }

    public List<FileChange> getChanges()
    {
        return Collections.unmodifiableList(changes);
    }

    public void reset()
    {
        changes.clear();
    }

    public void status(String message)
    {
        statusMessages.add(message);
    }

    public List<String> getStatusMessages()
    {
        return statusMessages;
    }

    public void checkCancelled() throws ScmCancelledException
    {

    }
}
