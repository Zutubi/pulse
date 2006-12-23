package com.zutubi.pulse.scm.cvs.client.commands;

import com.zutubi.pulse.core.model.Change;
import com.zutubi.pulse.scm.SCMCheckoutEventHandler;
import org.netbeans.lib.cvsclient.event.CVSAdapter;
import org.netbeans.lib.cvsclient.event.FileAddedEvent;

/**
 * <class comment/>
 */
public class CheckoutListener extends CVSAdapter
{
    private final SCMCheckoutEventHandler handler;

    public CheckoutListener(SCMCheckoutEventHandler handler)
    {
        if (handler == null)
        {
            throw new IllegalArgumentException("handler is a required argument.");
        }
        this.handler = handler;
    }

    public void fileAdded(FileAddedEvent e)
    {
        handler.fileCheckedOut(new Change(e.getFilePath(), null, Change.Action.ADD));
    }
}

