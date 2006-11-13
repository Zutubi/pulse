// Decompiled by Jad v1.5.8f. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   UpdateListener.java

package com.zutubi.pulse.scm.cvs.client.commands;

import com.zutubi.pulse.core.model.Change;
import com.zutubi.pulse.scm.SCMCheckoutEventHandler;
import org.netbeans.lib.cvsclient.event.CVSAdapter;
import org.netbeans.lib.cvsclient.event.FileAddedEvent;
import org.netbeans.lib.cvsclient.event.FileRemovedEvent;
import org.netbeans.lib.cvsclient.event.FileUpdatedEvent;

public class UpdateListener extends CVSAdapter
{
    private SCMCheckoutEventHandler handler;

    public UpdateListener(SCMCheckoutEventHandler handler)
    {
        this.handler = handler;
    }

    public void fileAdded(FileAddedEvent e)
    {
        if (handler != null)
        {
            handler.fileCheckedOut(new Change(e.getFilePath(), null, Change.Action.ADD));
        }
    }

    public void fileRemoved(FileRemovedEvent e)
    {
        if (handler != null)
        {
            handler.fileCheckedOut(new Change(e.getFilePath(), null, Change.Action.DELETE));
        }
    }

    public void fileUpdated(FileUpdatedEvent e)
    {
        if (handler != null)
        {
            handler.fileCheckedOut(new Change(e.getFilePath(), null, Change.Action.EDIT));
        }
    }
}
