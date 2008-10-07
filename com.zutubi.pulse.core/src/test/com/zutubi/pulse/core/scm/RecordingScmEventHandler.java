package com.zutubi.pulse.core.scm;

import com.zutubi.pulse.core.model.Change;
import com.zutubi.pulse.core.scm.api.ScmCancelledException;
import com.zutubi.pulse.core.scm.api.ScmEventHandler;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;


/**
 */
public class RecordingScmEventHandler implements ScmEventHandler
{
    private List<Change> changes = new LinkedList<Change>();

    private List<String> statusMessages = new LinkedList<String>();

    public void fileChanged(Change change)
    {
        changes.add(change);
    }

    public List<Change> getChanges()
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
