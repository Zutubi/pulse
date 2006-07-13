package com.zutubi.pulse.scm.cvs.client;

import com.zutubi.pulse.core.model.Change;
import org.netbeans.lib.cvsclient.event.*;

import java.util.List;

/**
 */
public class UpdateListener implements CVSListener
{
    private List<Change> changes;

    public UpdateListener(List<Change> changes)
    {
        this.changes = changes;
    }

    public void messageSent(MessageEvent e)
    {
    }

    public void messageSent(BinaryMessageEvent e)
    {
    }

    public void fileAdded(FileAddedEvent e)
    {
        changes.add(new Change(e.getFilePath(), null, Change.Action.ADD));
    }

    public void fileToRemove(FileToRemoveEvent e)
    {
    }

    public void fileRemoved(FileRemovedEvent e)
    {
        changes.add(new Change(e.getFilePath(), null, Change.Action.DELETE));
    }

    public void fileUpdated(FileUpdatedEvent e)
    {
        changes.add(new Change(e.getFilePath(), null, Change.Action.EDIT));
    }

    public void fileInfoGenerated(FileInfoEvent e)
    {
    }

    public void commandTerminated(TerminationEvent e)
    {
    }

    public void moduleExpanded(ModuleExpansionEvent e)
    {
    }
}
