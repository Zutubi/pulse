package com.zutubi.pulse.core.scm.cvs.client.commands;

import com.zutubi.pulse.core.scm.api.Change;
import com.zutubi.pulse.core.scm.api.ScmEventHandler;
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

