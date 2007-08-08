package com.zutubi.pulse.scm.cvs.client.commands;

import com.zutubi.pulse.core.model.Change;
import com.zutubi.pulse.scm.ScmEventHandler;
import org.netbeans.lib.cvsclient.event.CVSAdapter;
import org.netbeans.lib.cvsclient.event.FileAddedEvent;

/**
 * <class comment/>
 */
public class CheckoutListener extends CVSAdapter
{
    private final ScmEventHandler handler;

    public CheckoutListener(ScmEventHandler handler)
    {
        if (handler == null)
        {
            throw new IllegalArgumentException("handler is a required argument.");
        }
        this.handler = handler;
    }

    public void fileAdded(FileAddedEvent e)
    {
        handler.fileChanged(new Change(e.getFilePath(), null, Change.Action.ADD));
    }
}

