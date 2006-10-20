// Decompiled by Jad v1.5.8f. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   UpdateListener.java

package com.zutubi.pulse.scm.cvs.client.commands;

import com.zutubi.pulse.core.model.Change;
import java.util.LinkedList;
import java.util.List;
import org.netbeans.lib.cvsclient.event.*;

public class UpdateListener extends CVSAdapter
{
    public UpdateListener()
    {
        changes = new LinkedList<Change>();
    }

    public void fileAdded(FileAddedEvent e)
    {
        changes.add(new Change(e.getFilePath(), null, Change.Action.ADD));
    }

    public void fileRemoved(FileRemovedEvent e)
    {
        changes.add(new Change(e.getFilePath(), null, Change.Action.DELETE));
    }

    public void fileUpdated(FileUpdatedEvent e)
    {
        changes.add(new Change(e.getFilePath(), null, Change.Action.EDIT));
    }

    public List<Change> getChanges()
    {
        return changes;
    }

    private List<Change> changes;
}
